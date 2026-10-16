package com.vyoog.eisplatform.modules.profile.service;

import com.vyoog.eisplatform.common.exception.ForbiddenException;
import com.vyoog.eisplatform.common.exception.ResourceNotFoundException;
import com.vyoog.eisplatform.modules.audit.service.AuditService;
import com.vyoog.eisplatform.modules.auth.service.KeycloakAdminClient;
import com.vyoog.eisplatform.modules.authorization.service.AuthorizationService;
import com.vyoog.eisplatform.modules.platformadmin.service.PlatformAdminRegistry;
import com.vyoog.eisplatform.modules.profile.dto.ProfileDtos.Account;
import com.vyoog.eisplatform.modules.profile.dto.ProfileDtos.Address;
import com.vyoog.eisplatform.modules.profile.dto.ProfileDtos.Completion;
import com.vyoog.eisplatform.modules.profile.dto.ProfileDtos.CompletionResponse;
import com.vyoog.eisplatform.modules.profile.dto.ProfileDtos.Online;
import com.vyoog.eisplatform.modules.profile.dto.ProfileDtos.Personal;
import com.vyoog.eisplatform.modules.profile.dto.ProfileDtos.Photo;
import com.vyoog.eisplatform.modules.profile.dto.ProfileDtos.ProfileDto;
import com.vyoog.eisplatform.modules.profile.dto.ProfileDtos.ProfileUpdateRequest;
import com.vyoog.eisplatform.modules.profile.dto.ProfileDtos.Work;
import com.vyoog.eisplatform.modules.profile.model.CustomerPhoto;
import com.vyoog.eisplatform.modules.profile.repository.CustomerPhotoRepository;
import com.vyoog.eisplatform.modules.registration.model.Customer;
import com.vyoog.eisplatform.modules.registration.model.MembershipStatus;
import com.vyoog.eisplatform.modules.registration.model.Organization;
import com.vyoog.eisplatform.modules.registration.model.OrganizationMember;
import com.vyoog.eisplatform.modules.registration.model.RegistrationStatus;
import com.vyoog.eisplatform.modules.registration.repository.CustomerRepository;
import com.vyoog.eisplatform.modules.registration.repository.OrganizationMemberRepository;
import com.vyoog.eisplatform.modules.registration.repository.OrganizationRepository;
import com.vyoog.eisplatform.modules.registration.service.CurrentCustomerResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * "My profile" (C107): the person's own details, photo and completion, and the platform admin's edit of any
 * person's details. The caller is ALWAYS resolved from the JWT through {@link CurrentCustomerResolver}; no
 * method here takes a customer id for the self-service paths, so a person can never reach someone else's
 * profile. A platform admin may have no customer row (never forced to create one); the first save of the
 * form creates it.
 *
 * <p>Name changes are mirrored to Keycloak as the LAST step of the save, inside the same transaction: if
 * Keycloak refuses, the whole save (and its audit record) rolls back and the caller gets 502.
 *
 * <p>Audit records name only the sections that changed, never a value.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProfileService {

    /** The permission that makes someone a platform admin for profile purposes: the same one that guards
     * the admin registration endpoints. */
    static final String ADMIN_PERMISSION = "MANAGE_REGISTRATIONS";

    private final CurrentCustomerResolver customerResolver;
    private final CustomerRepository customerRepository;
    private final OrganizationMemberRepository memberRepository;
    private final OrganizationRepository organizationRepository;
    private final CustomerPhotoRepository photoRepository;
    private final ProfileValidator validator;
    private final AuditService auditService;
    private final KeycloakAdminClient keycloakAdminClient;
    private final AuthorizationService authorizationService;
    private final PlatformAdminRegistry platformAdminRegistry;

    /** Bytes and headers material of a stored photo. */
    public record PhotoContent(byte[] data, String contentType, Instant updatedAt) {
        public String etag() {
            return "\"" + updatedAt.toEpochMilli() + "-" + data.length + "\"";
        }
    }

    // ---- own profile ----

    public ProfileDto getMine(Jwt jwt, Collection<String> authorities) {
        requireUserToken(jwt);
        boolean admin = isPlatformAdmin(jwt, authorities);
        Optional<Customer> customer = customerResolver.resolveOptional(jwt);
        if (customer.isPresent()) {
            return toDto(customer.get(), admin);
        }
        requireAdminWithoutRecord(admin);
        return fromToken(jwt);
    }

    public CompletionResponse completionMine(Jwt jwt) {
        requireUserToken(jwt);
        Optional<Customer> customer = customerResolver.resolveOptional(jwt);
        if (customer.isEmpty()) {
            Completion none = PersonProfileCompleteness.empty();
            return new CompletionResponse(false, none.percent(), none.filled(), none.total(), none.missing());
        }
        Completion c = completion(customer.get());
        return new CompletionResponse(true, c.percent(), c.filled(), c.total(), c.missing());
    }

    @Transactional
    public ProfileDto updateMine(Jwt jwt, Collection<String> authorities, ProfileUpdateRequest request) {
        requireUserToken(jwt);
        boolean admin = isPlatformAdmin(jwt, authorities);
        ProfileUpdateRequest values = validator.validate(request);
        Optional<Customer> existing = customerResolver.resolveOptional(jwt);
        if (existing.isEmpty()) {
            requireAdminWithoutRecord(admin);
            return create(jwt, values);
        }
        Customer customer = existing.get();
        String oldFirst = customer.getFirstName();
        String oldLast = customer.getLastName();
        List<String> changed = apply(customer, values);
        if (!changed.isEmpty()) {
            customer.setProfileUpdatedAt(Instant.now());
            customerRepository.save(customer);
            audit("PROFILE_UPDATED", jwt.getSubject(), customer.getId(), customer, "sections changed: " + String.join(", ", changed));
            mirrorName(customer, oldFirst, oldLast);
        }
        return toDto(customer, admin);
    }

    // ---- photo (own profile only) ----

    @Transactional
    public ProfileDto putPhoto(Jwt jwt, Collection<String> authorities, MultipartFile file) {
        requireUserToken(jwt);
        Customer customer = ownRecordForPhoto(jwt, authorities);
        if (file == null || file.isEmpty()) {
            throw new ProfileException(HttpStatus.BAD_REQUEST, "Choose an image file to upload");
        }
        if (file.getSize() > ProfilePhotoInspector.MAX_BYTES) {
            throw new ProfileException(HttpStatus.PAYLOAD_TOO_LARGE, "The photo must be 1 MB or smaller");
        }
        byte[] bytes;
        try {
            bytes = file.getBytes();
        } catch (IOException e) {
            throw new ProfileException(HttpStatus.BAD_REQUEST, "The uploaded file could not be read");
        }
        String contentType = ProfilePhotoInspector.inspect(bytes, file.getContentType());
        CustomerPhoto photo = photoRepository.findById(customer.getId()).orElseGet(CustomerPhoto::new);
        photo.setCustomerId(customer.getId());
        photo.setContentType(contentType);
        photo.setSizeBytes(bytes.length);
        photo.setData(bytes);
        photo.setUpdatedAt(Instant.now());
        photoRepository.save(photo);
        audit("PROFILE_PHOTO_UPDATED", jwt.getSubject(), customer.getId(), customer, "photo replaced");
        return toDto(customer, isPlatformAdmin(jwt, authorities));
    }

    @Transactional
    public ProfileDto removePhoto(Jwt jwt, Collection<String> authorities) {
        requireUserToken(jwt);
        Customer customer = ownRecordForPhoto(jwt, authorities);
        if (photoRepository.existsById(customer.getId())) {
            photoRepository.deleteById(customer.getId());
            photoRepository.flush();
            audit("PROFILE_PHOTO_REMOVED", jwt.getSubject(), customer.getId(), customer, "photo removed");
        }
        return toDto(customer, isPlatformAdmin(jwt, authorities));
    }

    public PhotoContent getPhoto(Jwt jwt) {
        requireUserToken(jwt);
        Customer customer = customerResolver.resolveOptional(jwt)
            .orElseThrow(() -> new ResourceNotFoundException("No profile photo"));
        CustomerPhoto photo = photoRepository.findById(customer.getId())
            .orElseThrow(() -> new ResourceNotFoundException("No profile photo"));
        return new PhotoContent(photo.getData(), photo.getContentType(), photo.getUpdatedAt());
    }

    // ---- platform admin: any person's record ----

    public ProfileDto getForAdmin(Long customerId) {
        return toDto(findCustomer(customerId), false);
    }

    @Transactional
    public ProfileDto updateForAdmin(Long customerId, Jwt adminJwt, ProfileUpdateRequest request) {
        requireUserToken(adminJwt);
        Customer customer = findCustomer(customerId);
        ProfileUpdateRequest values = validator.validate(request);
        String oldFirst = customer.getFirstName();
        String oldLast = customer.getLastName();
        List<String> changed = apply(customer, values);
        if (!changed.isEmpty()) {
            customer.setProfileUpdatedAt(Instant.now());
            customerRepository.save(customer);
            Long adminCustomerId = customerResolver.resolveOptional(adminJwt).map(Customer::getId).orElse(null);
            auditService.recordSuccess("PROFILE_UPDATED_BY_ADMIN", adminJwt.getSubject(), adminCustomerId,
                adminJwt.getClaimAsString("email"), "Customer", String.valueOf(customer.getId()),
                organizationIdOf(customer), "sections changed: " + String.join(", ", changed));
            mirrorName(customer, oldFirst, oldLast);
        }
        return toDto(customer, false);
    }

    // ---- internals ----

    private ProfileDto create(Jwt jwt, ProfileUpdateRequest values) {
        String email = jwt.getClaimAsString("email");
        if (email == null || email.isBlank()) {
            throw new ProfileException(HttpStatus.BAD_REQUEST, "Your sign-in has no email address, so a profile cannot be created");
        }
        if (customerRepository.findByEmailIgnoreCase(email).isPresent()) {
            throw new ProfileException(HttpStatus.CONFLICT,
                "A customer record with your email address already exists and belongs to a different login");
        }
        Customer customer = new Customer();
        customer.setEmail(email.strip());
        customer.setStatus(RegistrationStatus.COMPLETED);
        customer.setKeycloakSub(jwt.getSubject());
        apply(customer, values);
        customer.setProfileUpdatedAt(Instant.now());
        try {
            customer = customerRepository.saveAndFlush(customer);
        } catch (DataIntegrityViolationException e) {
            throw new ProfileException(HttpStatus.CONFLICT,
                "A customer record with your email address already exists and belongs to a different login");
        }
        audit("PROFILE_CREATED", jwt.getSubject(), customer.getId(), customer, "profile created by the person");
        // The names come from the form, so tell Keycloak when they differ from what the login already says.
        mirrorName(customer, jwt.getClaimAsString("given_name"), jwt.getClaimAsString("family_name"));
        return toDto(customer, true);
    }

    /** Copies the validated values onto the customer and returns the sections whose values actually changed. */
    private List<String> apply(Customer c, ProfileUpdateRequest v) {
        Personal p = v.personal();
        Work w = v.work();
        Address a = v.address();
        Online o = v.online();
        boolean personal = !Objects.equals(c.getFirstName(), p.firstName()) | !Objects.equals(c.getLastName(), p.lastName())
            | !Objects.equals(c.getPreferredName(), p.preferredName()) | !Objects.equals(c.getMobile(), p.mobile())
            | !Objects.equals(c.getAlternatePhone(), p.alternatePhone()) | !Objects.equals(c.getBio(), p.bio());
        boolean work = !Objects.equals(c.getJobTitle(), w.jobTitle()) | !Objects.equals(c.getDepartment(), w.department())
            | !Objects.equals(c.getCompanyName(), w.companyName()) | !Objects.equals(c.getIndustry(), w.industry());
        boolean address = !Objects.equals(c.getAddressLine1(), a.addressLine1()) | !Objects.equals(c.getAddressLine2(), a.addressLine2())
            | !Objects.equals(c.getCity(), a.city()) | !Objects.equals(c.getState(), a.state())
            | !Objects.equals(c.getPostalCode(), a.postalCode()) | !Objects.equals(c.getCountry(), a.country());
        boolean online = !Objects.equals(c.getLinkedinUrl(), o.linkedinUrl()) | !Objects.equals(c.getWebsiteUrl(), o.websiteUrl());

        c.setFirstName(p.firstName());
        c.setLastName(p.lastName());
        c.setPreferredName(p.preferredName());
        c.setMobile(p.mobile());
        c.setAlternatePhone(p.alternatePhone());
        c.setBio(p.bio());
        c.setJobTitle(w.jobTitle());
        c.setDepartment(w.department());
        c.setCompanyName(w.companyName());
        c.setIndustry(w.industry());
        c.setAddressLine1(a.addressLine1());
        c.setAddressLine2(a.addressLine2());
        c.setCity(a.city());
        c.setState(a.state());
        c.setPostalCode(a.postalCode());
        c.setCountry(a.country());
        c.setLinkedinUrl(o.linkedinUrl());
        c.setWebsiteUrl(o.websiteUrl());

        List<String> changed = new ArrayList<>();
        if (personal) changed.add("personal");
        if (work) changed.add("work");
        if (address) changed.add("address");
        if (online) changed.add("online");
        return changed;
    }

    /** Names live in Keycloak too (the realm refuses a sign-in without them). Called last, so a failure rolls the save back. */
    private void mirrorName(Customer customer, String oldFirstName, String oldLastName) {
        if (Objects.equals(customer.getFirstName(), oldFirstName) && Objects.equals(customer.getLastName(), oldLastName)) {
            return;
        }
        if (customer.getKeycloakSub() == null || customer.getKeycloakSub().isBlank()) {
            return;
        }
        if (!keycloakAdminClient.updateUser(customer.getKeycloakSub(), customer.getFirstName(), customer.getLastName())) {
            throw new ProfileException(HttpStatus.BAD_GATEWAY,
                "Your details could not be saved because the sign-in system did not accept the update. Nothing was changed; please try again.");
        }
    }

    private void audit(String action, String actorSub, Long customerId, Customer customer, String detail) {
        auditService.recordSuccess(action, actorSub, customerId, customer.getEmail(), "Customer",
            String.valueOf(customer.getId()), organizationIdOf(customer), detail);
    }

    private Long organizationIdOf(Customer customer) {
        return membershipOf(customer).map(OrganizationMember::getOrganizationId).orElse(null);
    }

    /** The person's active membership, or else their most recent one (so a suspended member still shows an organization). */
    private Optional<OrganizationMember> membershipOf(Customer customer) {
        List<OrganizationMember> all = memberRepository.findByCustomerIdOrderByJoinedAtDesc(customer.getId());
        return all.stream().filter(m -> m.getStatus() == MembershipStatus.ACTIVE).findFirst()
            .or(() -> all.stream().findFirst());
    }

    private Customer findCustomer(Long customerId) {
        return customerRepository.findById(customerId)
            .orElseThrow(() -> new ResourceNotFoundException("Customer not found: " + customerId));
    }

    private Customer ownRecordForPhoto(Jwt jwt, Collection<String> authorities) {
        Optional<Customer> customer = customerResolver.resolveOptional(jwt);
        if (customer.isPresent()) {
            return customer.get();
        }
        if (!isPlatformAdmin(jwt, authorities)) {
            return customerResolver.resolve(jwt); // the platform's standard 404 for a login with no account
        }
        throw new ProfileException(HttpStatus.CONFLICT, "Create your profile before adding a photo");
    }

    private void requireAdminWithoutRecord(boolean admin) {
        if (!admin) {
            throw new ResourceNotFoundException("No Vyoog account is linked to this login");
        }
    }

    /**
     * A platform admin for profile purposes (C108): the login holds the admin permission, OR it is an enabled
     * entry of the platform admin registry file. The registry grants nothing else: only this recognition.
     * (An API key is refused by {@link #requireUserToken} before this is reached.)
     */
    private boolean isPlatformAdmin(Jwt jwt, Collection<String> authorities) {
        return authorizationService.hasPlatformPermission(authorities, ADMIN_PERMISSION)
            || platformAdminRegistry.isPlatformAdmin(jwt);
    }

    /** An API key is not a person: it can never read or change a profile. */
    private static void requireUserToken(Jwt jwt) {
        if (jwt.hasClaim("api_key_id")) {
            throw new ForbiddenException("An API key cannot be used for a personal profile");
        }
    }

    /**
     * Whether the person's names may be edited here. Investigated against FederatedAccountService
     * (C107): SAML and OIDC sign-ins copy the identity provider's names only when they JIT-provision a brand
     * new account; an existing account's names are never overwritten at a later sign-in. Nothing else
     * rewrites names, so they are always editable here. If a sign-in ever starts syncing names from an
     * identity provider, this is the single place to return false for those accounts.
     */
    private static boolean nameEditable(Customer customer) {
        return true;
    }

    private Completion completion(Customer customer) {
        return PersonProfileCompleteness.of(customer, photoRepository.findMetaByCustomerId(customer.getId()).isPresent());
    }

    private ProfileDto toDto(Customer c, boolean platformAdmin) {
        Optional<OrganizationMember> membership = membershipOf(c);
        Organization organization = membership.flatMap(m -> organizationRepository.findById(m.getOrganizationId())).orElse(null);
        String role = platformAdmin ? "PLATFORM_ADMIN"
            : membership.map(m -> m.getOrgRole().name()).orElse("INDIVIDUAL");
        Account account = new Account(c.getEmail(), role,
            organization == null ? null : organization.getName(),
            organization == null ? null : organization.getCode(),
            membership.map(m -> m.getStatus().name()).orElse(null),
            membership.map(OrganizationMember::getJoinedAt).orElse(c.getCreatedAt()),
            nameEditable(c));
        var photoMeta = photoRepository.findMetaByCustomerId(c.getId());
        return new ProfileDto(true, account,
            new Personal(c.getFirstName(), c.getLastName(), c.getPreferredName(), c.getMobile(), c.getAlternatePhone(), c.getBio()),
            new Work(c.getJobTitle(), c.getDepartment(), c.getCompanyName(), c.getIndustry()),
            new Address(c.getAddressLine1(), c.getAddressLine2(), c.getCity(), c.getState(), c.getPostalCode(), c.getCountry()),
            new Online(c.getLinkedinUrl(), c.getWebsiteUrl()),
            new Photo(photoMeta.isPresent(), photoMeta.map(m -> m.getUpdatedAt()).orElse(null)),
            PersonProfileCompleteness.of(c, photoMeta.isPresent()));
    }

    /** A signed-in platform admin with no customer row: everything the login itself knows, nothing else. */
    private ProfileDto fromToken(Jwt jwt) {
        return new ProfileDto(false,
            new Account(jwt.getClaimAsString("email"), "PLATFORM_ADMIN", null, null, null, null, true),
            new Personal(jwt.getClaimAsString("given_name"), jwt.getClaimAsString("family_name"), null, null, null, null),
            new Work(null, null, null, null),
            new Address(null, null, null, null, null, null),
            new Online(null, null),
            new Photo(false, null),
            PersonProfileCompleteness.empty());
    }
}
