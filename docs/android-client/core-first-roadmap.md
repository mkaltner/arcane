# Android Core-First Roadmap

## Goal

Carry the Android client forward with Arcane's existing product feel while prioritizing the operational core first. The first usable version should feel familiar to Arcane users, but it should avoid broad surface-area work until the connect/auth/environment/container/project path is solid.

## Product direction

- Match Arcane's clean operations-console feel: dark-first, card-based, status-forward, compact but readable.
- Favor practical system state over marketing chrome.
- Make safe day-to-day actions easy: inspect, start, stop, restart.
- Keep destructive and editing workflows out of the MVP.
- Keep UI choices consistent enough with the web app that users recognize Arcane immediately.

## Phase 1 — Core vertical slice

Purpose: prove a useful end-to-end Android client against an Arcane Manager.

1. Connect to server
   - Normalize user-entered Manager URL.
   - Derive API base as `<origin>/api`.
   - Persist the last successful server URL.
   - Show specific validation errors for malformed or unreachable URLs.

2. Authenticate
   - Support local username/password login first.
   - Add API-key mode only if it stays small and does not delay the main path.
   - Store auth material securely.
   - Preserve server URL when auth expires.

3. Select environment
   - Fetch environments.
   - Default to environment `0` when present and no prior choice exists.
   - Persist selected environment per server.

4. Home dashboard
   - Show selected server/environment.
   - Show health/dashboard summary.
   - Provide shortcuts to Containers and Projects.

5. Containers
   - List containers with name, image, and status.
   - Detail screen with safe lifecycle actions.
   - Confirm stop/restart.
   - Refresh after actions.

6. Projects
   - List projects with status/runtime summary.
   - Detail screen with safe lifecycle actions.
   - Confirm down/restart.
   - Refresh after actions.

## Phase 1 visual baseline

- Dark-first theme aligned with Arcane's admin-console tone.
- Rounded Material 3 cards for resource/status summaries.
- Clear status chips; never color-only.
- Bottom navigation or top-level tabs for Home, Containers, Projects, Settings.
- Empty/error/loading states for every network-backed screen.
- Dense enough for operations, but touch targets remain Android-friendly.

## Phase 2 — Flesh-out phase

Only after the core path is manually verified on an emulator/device:

- Improve visual polish and spacing across phone sizes.
- Add richer resource details where API support is straightforward.
- Add search/filter/sort on lists.
- Add read-only logs if the API path is low-risk.
- Add Images, Volumes, Networks, and Activity as secondary tabs/screens.
- Add onboarding niceties such as recent servers and clearer first-run copy.
- Add tablet/large-screen layout improvements.

Still deferred after Phase 2 unless explicitly prioritized:

- Create/edit/delete resource workflows.
- Terminal/exec access.
- Destructive prune/remove/delete actions.
- Push notifications.
- Play Store packaging.

## Immediate implementation order

1. Core URL model and normalization tests.
2. Settings persistence for server URL and selected environment.
3. API client error model and auth header provider.
4. Connect screen wired to the URL model.
5. Auth repository and login screen.
6. Environment repository and picker/defaulting.
7. Dashboard fetch and Home UI.
8. Containers list/detail/actions.
9. Projects list/detail/actions.

## Verification gates

Before marking Phase 1 complete:

- Unit tests pass for URL normalization, environment defaulting, auth header selection, and error mapping.
- `./gradlew testDebugUnitTest lintDebug assembleDebug` succeeds.
- App launches in emulator.
- User can connect to local Arcane Manager from emulator via `http://10.0.2.2:3552`.
- Auth, environment selection, dashboard, containers, and projects are manually exercised.

## AI disclosure

This roadmap was drafted with Hermes Agent assistance and should be human-reviewed before upstream submission.
