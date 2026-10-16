# UI requirements — My profile
Screen spec: [profile.md](../../../05-ui/screen-requirements/profile.md). Layout changes (profile menu, global bar): [application-layout.md](../../../05-ui/screen-requirements/application-layout.md). Route `/account/profile` (`RequireAuth`, lazy page `ProfilePage`); the admin "Edit profile" dialog is opened from the organization detail members list and the individuals list.

No frontend change for the platform administrator registry ([C108](../../../01-business/roadmap/open-decisions.md#c108)): a registry-listed login gets the same `exists:false` answer as a holder of `MANAGE_REGISTRATIONS` and so sees the existing optional "Create my profile" banner instead of the load-error state; no new text, route or component.
