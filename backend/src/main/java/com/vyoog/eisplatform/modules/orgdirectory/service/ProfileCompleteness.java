package com.vyoog.eisplatform.modules.orgdirectory.service;

import com.vyoog.eisplatform.modules.orgdirectory.dto.OrgDirectoryDtos.Completion;
import com.vyoog.eisplatform.modules.registration.model.Customer;
import com.vyoog.eisplatform.modules.registration.model.Organization;

import java.util.ArrayList;
import java.util.List;

/** Profile completion rules BR-DIR-003 to BR-DIR-005 (computed, never stored). */
final class ProfileCompleteness {

    private ProfileCompleteness() {
    }

    static boolean filled(String value) {
        return value != null && !value.isBlank();
    }

    static Completion organization(Organization o, boolean hasAdminContact) {
        List<String> missing = new ArrayList<>();
        check(missing, "TYPE", filled(o.getType()));
        check(missing, "INDUSTRY", filled(o.getIndustry()));
        check(missing, "WEBSITE", filled(o.getWebsite()));
        check(missing, "PHONE", filled(o.getPhone()));
        check(missing, "COUNTRY", filled(o.getCountry()));
        check(missing, "STATE", filled(o.getState()));
        check(missing, "CITY", filled(o.getCity()));
        check(missing, "ADDRESS", filled(o.getAddress()));
        check(missing, "TAX_REGISTRATION", filled(o.getGstin()) || filled(o.getPan())
            || filled(o.getCompanyRegistrationNumber()) || filled(o.getTaxVatNumber()));
        boolean billing = o.isBillingSameAsAddress()
            ? filled(o.getAddress())
            : filled(o.getBillingAddress()) && filled(o.getBillingCountry());
        check(missing, "BILLING_ADDRESS", billing);
        check(missing, "REGION", o.getRegionId() != null);
        check(missing, "ADMIN_CONTACT", hasAdminContact);
        return new Completion(percent(12, missing.size()), missing);
    }

    static Completion individual(Customer c) {
        List<String> missing = new ArrayList<>();
        check(missing, "MOBILE", filled(c.getMobile()));
        check(missing, "COUNTRY", filled(c.getCountry()));
        check(missing, "COMPANY_NAME", filled(c.getCompanyName()));
        check(missing, "JOB_TITLE", filled(c.getJobTitle()));
        check(missing, "INDUSTRY", filled(c.getIndustry()));
        return new Completion(percent(5, missing.size()), missing);
    }

    private static void check(List<String> missing, String code, boolean ok) {
        if (!ok) {
            missing.add(code);
        }
    }

    private static int percent(int total, int missing) {
        return Math.round(100f * (total - missing) / total);
    }
}
