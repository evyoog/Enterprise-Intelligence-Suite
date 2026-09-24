# Functional Requirements Documents (per feature)

One folder per feature, in kebab-case (e.g. `shift-change/`, `org-registration/`). Each feature folder holds the full, approved definition of that feature:

```
FRD/<feature>/
├── requirement.md          # REQ-<APP-CODE>-<NNN> — what and why
├── business-rules.md       # rules the feature must enforce
├── workflow.md             # states, transitions, actors
├── ui-requirements.md      # screens, fields, validation
├── api-requirements.md     # endpoints, payloads, errors
└── acceptance-criteria.md  # Given/When/Then — source for test cases
```

Copy `_template/` to start a new feature. Nothing is implemented until its FRD is marked **Approved**.
