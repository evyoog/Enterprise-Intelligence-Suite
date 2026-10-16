# Platform administrator registry

**Status:** Built 2026-10-16, decision [C108](../../01-business/roadmap/open-decisions.md#c108) (product owner request). Requirement: [REQ-TEN-011.16 and .17](../../02-requirements/FRD/my-profile/requirement.md); rules [BR-PRF-026 to BR-PRF-032](../../02-requirements/FRD/my-profile/business-rules.md); acceptance criteria AC-19 to AC-29; tests [TESTPLAN-TEN-011](../../../test-cases/functional/my-profile/TESTPLAN-TEN-011.md) (TC-TEN-139 onwards). The defaults the owner did not state (RG-1 to RG-6) are marked "default, to confirm" in C108 and in the headings below.

## 1. Purpose
A platform administrator who saved the My profile page got "No Vyoog account is linked to this login" (HTTP 404). The profile treated a person as a platform administrator only when their sign-in resolved, through the permission matrix, to a platform role holding `MANAGE_REGISTRATIONS`; for that login it did not, and a login with no customer record that is not a platform administrator is refused. Platform administrators often have no customer record at all.

The registry is **one JSON file that lists the platform administrator accounts**. A person listed in it (and enabled) is a platform administrator for the profile pages and is not refused. The file is stored in this project today; it will be moved to an S3 folder in AWS and the application then reads it from there (section 11).

## 2. At a glance
| Question | Answer |
|---|---|
| What does the file contain? | **Who** is a platform administrator: email, Keycloak user id, display name, enabled flag, notes. |
| Does it contain passwords? | **Never.** Not a password, not a token, not any secret ([RG-1](../../01-business/roadmap/open-decisions.md#c108)). |
| How does a listed person sign in? | With their own Keycloak account (realm `eVyoog`), as everyone does. The registry only *recognises* them after they have signed in. |
| What does being listed do? | On `/me/profile`, `/me/profile/completion` and `/me/profile/photo` the person counts as a platform administrator: no 404, they may create their own customer record on the first save (optional), their `account.role` is `PLATFORM_ADMIN`. |
| What does it **not** do? | Nothing else. No permission, no role, no `/admin/**` access, no change to `SecurityConfig`. |
| Where is it read from? | `eis.platform-admins.location`; default `classpath:config/platform-admins.json` (this project); `file:` and `s3://bucket/key` also work. |
| How fast does a change apply? | Within the refresh interval (60 seconds by default) of the next profile call, with no redeploy, **when the file is outside the jar** (`file:` or S3). The classpath copy is inside the built jar, so changing it needs a new build and deploy. |
| If it breaks? | Fail closed: if the first load fails nobody extra is an administrator; if a later refresh fails the last good copy stays in use. |

## 3. The file
Path in this project: `backend/src/main/resources/config/platform-admins.json`.

```json
{
  "version": 1,
  "description": "Platform administrator registry. Lists identities only, never passwords or secrets.",
  "platformAdmins": [
    {
      "email": "admin@your-company.example",
      "keycloakSub": "00000000-0000-0000-0000-000000000000",
      "displayName": "Example Administrator",
      "enabled": true,
      "notes": "Placeholder. Identified by Keycloak user id; the email is kept for reading convenience."
    },
    {
      "email": "second.admin@your-company.example",
      "keycloakSub": "",
      "displayName": "Second Administrator",
      "enabled": true,
      "notes": "Keycloak user id not known yet: matched by verified email instead."
    },
    {
      "email": "former.admin@your-company.example",
      "keycloakSub": "11111111-1111-1111-1111-111111111111",
      "displayName": "Former Administrator",
      "enabled": false,
      "notes": "Disabled on 2026-10-16, kept as a record."
    }
  ]
}
```

| Field | Meaning |
|---|---|
| `version` | Format version. `1` today. |
| `description` | Free text for the reader. |
| `platformAdmins` | The list of entries. |
| `email` | The administrator's login email. Compared with the token's `email` claim **ignoring letter case**, and only when the token's `email_verified` claim is true. |
| `keycloakSub` | The Keycloak user id (a UUID), the person's `sub` in the sign-in token. **Preferred and robust key**: it does not change when an email changes and cannot be asserted by another identity provider. Empty or blank means "no id": such an entry never matches by id. |
| `displayName` | A name for people reading the file. Not used for matching and not copied into any customer record. |
| `enabled` | `true` or `false`. A missing `enabled` means `true`. A disabled entry never matches. |
| `notes` | Free text (who approved it, when, why). |

Reading rules ([BR-PRF-032](../../02-requirements/FRD/my-profile/business-rules.md)): unknown properties are ignored (so the file can carry extra remarks without breaking); a missing `enabled` means true; duplicate entries are harmless; the file must be valid JSON (an invalid file is handled as in section 9). **Do not put a password, token, API key or any other secret in this file**, in `notes` or anywhere else.

### The file shipped in this project (RG-6, default, to confirm)
It contains no real identity: version 1, a short `description`, and **one example entry that is disabled**, with the email `replace-with-admin@your-company.example` and an empty `keycloakSub`. A fresh install therefore recognises nobody. The product owner replaces the example with real entries.

## 4. How a person signs in
The registry is not a login. Passwords stay in Keycloak (`https://user.evyoog.com`, realm `eVyoog`), which also holds MFA. A listed person opens the application, signs in with their Keycloak account (directly or through a federated identity provider), and the first profile call carries the sign-in token; the application compares the token with the registry. If you want a new administrator, they first need a Keycloak account; then you add them to the file. Do not ask for, store or send their password.

## 5. Finding a person's Keycloak user id (`keycloakSub`)
Any one of these:
1. **Keycloak admin console:** realm `eVyoog` → Users → search by email → open the user → the **ID** shown at the top of the Details tab is the `sub` (a UUID).
2. **Keycloak admin REST API** (needs an administrator): `GET /admin/realms/eVyoog/users?email=<email>&exact=true`; the `id` field of the result.
3. **From the person's own sign-in token:** the `sub` claim of the access token. Decode it locally (for example in your own tooling); do not paste a live token into an online decoder, because a token is a credential.

Check that the value is a UUID with no spaces before saving the file.

## 6. Matching rules (RG-3, default, to confirm)
An entry matches a sign-in when **`enabled` is true and either**:
- its `keycloakSub` is not blank **and** equals the token's `sub`; **or**
- its `email` equals the token's `email` claim, ignoring letter case, **and** the token's `email_verified` claim is `true`.

Consequences: a blank `keycloakSub` never matches; a disabled entry never matches; an email that the identity provider has not verified never matches; an API key (token claim `api_key_id`) is **never** matched because the existing refusal of API keys on the profile endpoints runs first (403). Prefer `keycloakSub`.

## 7. What it grants and what it does not (RG-2, default, to confirm)
| Area | Effect of being listed |
|---|---|
| `GET /me/profile` with no customer record | 200 with `exists:false`, built from the token (instead of 404). |
| `PUT /me/profile` with no customer record | Creates their own record (optional, never forced; same rules as before: email from the token, 409 if the email already belongs to another login, audit `PROFILE_CREATED`). |
| `PUT`/`DELETE /me/profile/photo` | Allowed once the record exists; 409 "Create your profile before adding a photo" before. |
| `account.role` shown | `PLATFORM_ADMIN`. |
| `GET /me/profile/completion` | Unchanged (it was never 404). |
| `SecurityConfig`, permissions, roles | **Unchanged.** |
| `/admin/**` (including `/admin/registrations/individuals/{customerId}/profile`) | **Unchanged**: still needs `MANAGE_REGISTRATIONS` through Keycloak roles and the permission matrix. |
| Other `/me/**` endpoints that need a customer record (for example preferences) | **Unchanged**: still need a record (see follow-ups). |

## 8. Security rationale
- **No secrets in the file.** The repository rule is no credentials in code or configuration ([BR-SEC-001](../../03-business-rules/BR-SEC-001-central-secrets-file.md), `CLAUDE.md` rule 7). The product owner's request spoke of "platform admin accounts"; storing passwords for them is explicitly refused (RG-1). The file is readable by anyone who can read the repository or the S3 object, and it travels in builds and logs of deployments; identities are low-risk, passwords are not.
- **The effect is narrow on purpose.** A federated identity provider (SAML or OIDC) can assert an email address for a person. If a listed email granted authorization, anyone able to make an identity provider assert that address could become an administrator. So the registry grants only the profile behaviour of section 7; authorization stays with Keycloak roles and the permission matrix. Email matches additionally require `email_verified=true`.
- **Fail closed.** An empty or unreadable registry makes nobody extra an administrator.
- **No content in logs.** Log lines name only the source type, the error class and a count (section 9); never file content, never an email.
- **S3 access is least privilege** (section 11): read one object, through an IAM role, never an access key.
- **Changing the file is a privileged act**: whoever can edit the file decides who the application treats as a platform administrator for the profile pages. Restrict write access to the repository path and, later, to the S3 object (who exactly may write it is a process decision for the product owner; not specified).

## 9. Configuration, refresh and failure
| Key | Environment variable | Default | Meaning |
|---|---|---|---|
| `eis.platform-admins.location` | `EIS_PLATFORM_ADMINS_LOCATION` | `classpath:config/platform-admins.json` | `classpath:...`, `file:/absolute/path.json` or `s3://bucket/key`. A blank value falls back to the default |
| `eis.platform-admins.refresh-seconds` | `EIS_PLATFORM_ADMINS_REFRESH_SECONDS` | `60` | Minimum time between two reads of the source; a smaller value is raised to 1 second |
| `eis.platform-admins.s3.region` (S3 only) | `EIS_PLATFORM_ADMINS_S3_REGION` | empty | AWS region of the bucket; blank lets the AWS SDK's region chain apply (set it explicitly anyway) |
| `eis.platform-admins.s3.endpoint` (S3 only) | `EIS_PLATFORM_ADMINS_S3_ENDPOINT` | empty | Optional S3-compatible endpoint (MinIO or localstack, for development) |

The keys sit under the existing `eis:` tree of `backend/src/main/resources/application.yml`, set from environment variables only, with **no secret and no key as default**. S3 is read with the AWS SDK already used by the knowledge media storage, with the **default AWS credential provider chain** (task role or instance role); the registry has no access-key setting. The same list is in [aws-s3.md](../../09-integrations/aws-s3.md#platform-administrator-registry-eisplatform-admins).

**Refresh (RG-5, default, to confirm).** The source is re-read at most every `refresh-seconds`, lazily when the registry is used (no scheduler). A change is therefore visible within about that interval of the next profile call, with no redeploy (for `file:` and S3 sources). The list is held in memory and replaced as a whole. The first load blocks other callers until it finishes; a later refresh never blocks: other callers keep using the current list while one caller re-reads. A failed first load is retried only after the next interval.

**Failure.**
| Situation | Behaviour |
|---|---|
| The very first load fails (source unreadable, object missing, access denied, invalid JSON) | The registry is **empty**: nobody extra is a platform administrator. |
| The location itself is unusable (unsupported scheme, `s3://` with no bucket or no key) | Does **not** stop the application from starting. The source reports an error, handled like any failing source: first load, the registry stays empty; refresh, the last good list is kept. A WARN line names only the source type and the error class. |
| A later refresh fails or the JSON is invalid | The **last good copy is kept**; the next attempt happens after the next interval. |
| Logging | Log lines carry only the source type (`classpath`, `file` or `s3`), the error class and the number of enabled entries: a WARN line with the source type and the error class; one INFO line at start and whenever the content changes with the number of enabled entries and the source type. Never the content, never an email. |

The exact wording of the log lines is set by the code; look for the registry lines near start-up and for the source type `s3` once moved.

## 10. Operations
- **Add an administrator:** make sure they have a Keycloak account; find their `sub` (section 5); add an entry with `enabled: true`; save. Source in the project: commit, build and deploy. Source `file:` or S3: replace the file/object; it applies within the refresh interval after the next profile call. Ask the person to open My profile (a reload is enough).
- **Disable an administrator:** set `"enabled": false` (keeps the record) or delete the entry. Takes effect within the refresh interval. Their customer record, if they created one, is **not** touched, and nothing in Keycloak changes; removing a person's authorization is done in Keycloak and the permission matrix, not here.
- **Validate before saving:** the file must be valid JSON (for example `python3 -m json.tool platform-admins.json`). An invalid edit is ignored by a running application (last good copy kept, WARN logged) but on a fresh start leaves the registry empty.
- **Review:** keep `notes` current (who approved, when); remove entries that are no longer needed.

## 11. Moving the file to S3 (step by step)
The only thing that changes is the location. The file format, the rules and the code do not. Use placeholders below: `<bucket>`, `<key>`, `<region>`, `<account-id>`, `<task-role-name>`.

1. **Choose the bucket and key.** A private bucket, or a prefix in an existing private bucket, used only for configuration; for example `s3://<bucket>/config/platform-admins.json`. A separate bucket, or a dedicated prefix in an existing private bucket (not the knowledge media prefixes, see [aws-s3.md](../../09-integrations/aws-s3.md)), keeps the IAM policy to one object.
2. **Create the bucket (if new) private.**
   ```bash
   aws s3api create-bucket --bucket <bucket> --region <region> \
     --create-bucket-configuration LocationConstraint=<region>
   aws s3api put-public-access-block --bucket <bucket> \
     --public-access-block-configuration BlockPublicAcls=true,IgnorePublicAcls=true,BlockPublicPolicy=true,RestrictPublicBuckets=true
   aws s3api put-bucket-encryption --bucket <bucket> \
     --server-side-encryption-configuration '{"Rules":[{"ApplyServerSideEncryptionByDefault":{"SSEAlgorithm":"AES256"}}]}'
   aws s3api put-bucket-versioning --bucket <bucket> --versioning-configuration Status=Enabled
   ```
   All four Block Public Access settings on, default encryption (SSE-S3 shown; with SSE-KMS the role also needs `kms:Decrypt` on the key), and versioning on so a bad edit can be rolled back. (For `us-east-1` omit the `LocationConstraint`.)
3. **Upload the same JSON.** Validate it first (`python3 -m json.tool platform-admins.json`), then:
   ```bash
   aws s3 cp platform-admins.json s3://<bucket>/<key> --sse AES256
   ```
   Put your real entries in this copy; do not put secrets in it.
4. **Give the application read access to exactly that object.** The application uses the default AWS credential chain, so attach this policy to the role the backend runs under (the ECS **task role**, or the EC2 instance role while the service runs on the EC2 launch type):
   ```json
   {
     "Version": "2012-10-17",
     "Statement": [
       {
         "Sid": "ReadPlatformAdminRegistry",
         "Effect": "Allow",
         "Action": "s3:GetObject",
         "Resource": "arn:aws:s3:::<bucket>/<key>"
       }
     ]
   }
   ```
   ```bash
   aws iam put-role-policy --role-name <task-role-name> \
     --policy-name platform-admin-registry-read --policy-document file://policy.json
   ```
   No `s3:PutObject`, no `s3:ListBucket`, no wildcard. Note: without `s3:ListBucket` S3 answers a missing key with "access denied"; that is only a diagnostic detail. Do not attach this to the task **execution** role (that role pulls images and writes logs; it is not the application's identity). `deployment/ecs/task-definition.json` today names only an `executionRoleArn`; to use a task role, add `taskRoleArn` there and register the change on purpose as described in [deployment/ecs/README.md](../../../deployment/ecs/README.md) (`FORCE_REGISTER=true`), or rely on the instance role.
5. **Point the application at it.** Set in the backend's environment (for example the `environment` list of the backend container in the task definition; none of these is a secret):
   ```
   EIS_PLATFORM_ADMINS_LOCATION=s3://<bucket>/<key>
   EIS_PLATFORM_ADMINS_S3_REGION=<region>
   ```
   (If the region variable is blank the AWS SDK's own region chain applies; set it explicitly.) `EIS_PLATFORM_ADMINS_S3_ENDPOINT` stays empty on AWS (use it only for MinIO or localstack). `EIS_PLATFORM_ADMINS_REFRESH_SECONDS` stays at 60 unless you want another interval. Restart or redeploy the backend once so it reads the new location.
6. **Verify.**
   - In the backend log after start, find the registry INFO line: the source type must be `s3` and the count of enabled entries must be the number you expect. A WARN line instead (source type `s3` and an error class such as access denied or no such key) means step 3, 4 or 5 is wrong; until fixed the registry is empty (fail closed).
   - Sign in as a listed administrator who has no customer record and open `/account/profile`: the optional "Create my profile" banner must show, not an error.
   - Sign in as someone not listed and with no record: they still get "No Vyoog account is linked to this login".
7. **Prove a change without a redeploy.** Set one entry to `enabled: false` in the S3 object, wait one refresh interval (60 seconds) and reload the profile as that person: they are no longer recognised. Restore the entry.
8. **Retire the project copy.** After the check, leave `backend/src/main/resources/config/platform-admins.json` as the shipped **placeholder** (one disabled example, no real identity): with an `s3://` location it is not read. Do not keep real entries in two places; the S3 object is then the only source. To go back, unset `EIS_PLATFORM_ADMINS_LOCATION` (the default is the project file) and redeploy.

**Rollback / break glass.** A bad S3 edit: restore the previous object version (versioning), or upload the last good file; a running application keeps its last good copy meanwhile. Access to the S3 object being lost on a running application: the last good copy stays; on a restart the registry is empty until access is restored, and a person with `MANAGE_REGISTRATIONS` is unaffected (permission-based recognition does not depend on the registry).

**What the automated tests cover and do not:** the S3 reading is tested through a small injectable seam (bucket and key parsing, a malformed location, a failing fetch that keeps the last good list), not against a live bucket; the live read is a manual check (step 6, TC-TEN-151).

## 12. Troubleshooting: "No Vyoog account is linked to this login"
This message means: the login has no customer record, does not hold `MANAGE_REGISTRATIONS` and is not recognised by the registry. Check in this order:
1. Is the person's entry in the file the application is **actually reading**? Check `EIS_PLATFORM_ADMINS_LOCATION` (the default is the project file inside the built jar; an edit to the source file is not seen until a new build is deployed).
2. Is the entry `enabled` (not `false`)? Is the JSON valid (a refresh that fails keeps the old copy, so a recent edit may not be live)? Look for the registry WARN line.
3. Does the entry match? `keycloakSub` equals the person's Keycloak user id exactly (a UUID, no spaces)? Or, with no id, does `email` equal the login email (case does not matter) **and** is the email verified in Keycloak (`email_verified` true)? A federated login whose provider does not send a verified email only matches by `keycloakSub`.
4. Has the refresh interval passed since the file was changed (60 seconds by default), and has the person made a new profile call?
5. Is the call made with an **API key**? API keys are always refused (403) and never matched.
6. Did startup log the expected number of enabled entries and the expected source type? If the count is 0, the first load failed or the entries are disabled: the registry is empty (fail closed).
7. For S3: does the task or instance role hold `s3:GetObject` on exactly that object, is the region right, and is the object key spelled exactly as in the location?
8. Is the message coming from another `/me/**` endpoint (for example preferences) that needs a customer record? The registry does not change those (follow-ups).
9. Does the person actually need the registry? A holder of `MANAGE_REGISTRATIONS` is recognised without it; if that person is refused, the problem is their roles in Keycloak and the permission matrix, not this file.

## 13. Follow-ups the product owner may want (not built)
- **Widening the effect.** The registry grants the profile behaviour only. Letting it grant anything else (for example admin screens) would be a separate decision with its own security review; federated email assertion (section 8) is the main risk.
- **An admin screen** to add, disable and review administrators, with audit, instead of editing a file.
- **Other `/me/**` endpoints** (for example preferences) still need a customer record; a registry administrator without one is still refused there. Options: let those endpoints behave like the profile, or create the record at first sign-in.
- **Who may write the S3 object** and a review rhythm for the list.
- **Alerting** when the registry is empty or a refresh keeps failing.
- Confirm or change the defaults RG-1 to RG-6 in [C108](../../01-business/roadmap/open-decisions.md#c108).
