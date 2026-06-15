# Android MVP Product Spec

## Product goal

Build a focused Android companion for Arcane that lets an authenticated user connect to an Arcane Manager, inspect Docker environments, view core Docker resources, and perform safe day-to-day lifecycle actions without opening the web UI.

The MVP is intentionally operational and read-heavy. It should prove the connection/auth/API model and a first useful vertical slice before expanding into resource creation, editing, terminal access, or destructive maintenance workflows.

## Target user

- Existing Arcane users who manage Docker hosts from a phone.
- Homelab/server operators who want quick status checks and safe restart/start/stop actions.
- Contributors evaluating whether Arcane's API supports a native mobile client.

## MVP scope

In scope:

- Add/connect to an Arcane Manager by URL.
- Authenticate with local Arcane credentials or manually supplied API key if chosen.
- Select a Docker environment; default to local environment `0` when appropriate.
- View dashboard/health summary.
- View container list and container detail.
- Start, stop, and restart containers with confirmation.
- View project list and project detail/runtime state.
- Start/up, down, and restart projects with confirmation.
- Handle loading, empty, offline, auth-expired, unauthorized, and server-error states.
- Persist server URL, selected environment, and auth material using Android-secure storage.

Out of scope for MVP:

- Container/project creation and editing.
- Delete/destroy/prune operations.
- Terminal/exec access.
- Full logs UI, unless trivial read-only logs are added after the vertical slice.
- Volume file browsing, backups, and restores.
- Swarm management.
- User/admin/settings management.
- Push notifications.
- Publishing to Play Store.

## Navigation model

Recommended first navigation:

1. **Launch / Server setup**
2. **Login / API key setup**
3. **Environment picker** if more than one environment exists
4. **Home / Dashboard**
5. Bottom navigation or top-level tabs:
   - Containers
   - Projects
   - Settings

Optional later tabs:

- Images
- Volumes
- Networks
- Activity

## First launch flow

1. Show welcome/connect screen.
2. User enters Arcane Manager URL.
3. Normalize URL:
   - trim whitespace,
   - remove trailing slash,
   - keep scheme explicit,
   - derive API base as `${origin}/api`.
4. Validate server reachability.
5. If server responds and auth is required, navigate to login/API key.
6. If URL is invalid, TLS fails, or server is unreachable, show specific corrective messaging.

Acceptance criteria:

- User can enter `https://example.com` or `http://host:3552`.
- The app does not silently assume a cloud endpoint.
- Errors distinguish malformed URL, network unreachable, TLS/certificate problem, and non-Arcane server response when possible.

## Authentication flow

Preferred MVP path:

1. User enters username/password for local Arcane auth.
2. App calls login endpoint.
3. App stores returned auth material securely.
4. App calls current-user endpoint to confirm identity.
5. App refreshes auth when required.
6. If refresh fails, app returns to login and preserves server URL.

Alternative/advanced path:

- User enters an Arcane API key and the client sends `X-Api-Key`.

Acceptance criteria:

- Auth material is stored only in encrypted Android storage.
- Logout clears local auth material.
- Expired sessions lead to re-authentication, not ambiguous generic errors.
- The app does not rely on browser-only cookie behavior.

## Environment flow

1. After authentication, fetch environments.
2. If exactly one environment is available, select it automatically.
3. If local environment `0` is available and no prior selection exists, default to it.
4. If multiple environments are available, show an environment picker with health/status indicators where available.
5. Persist selected environment per server.
6. If the selected environment later fails, keep it selected but surface offline/error status and allow switching.

Acceptance criteria:

- All Docker resource calls include the selected environment ID.
- Users can change environments without deleting server/auth setup.
- Environment-unavailable errors are not confused with app-wide server failures.

## Home / dashboard screen

Purpose:

- Quick answer to: “Is my Arcane server up and what needs attention?”

Content:

- Current server and environment.
- Environment health / version if available.
- Summary cards for containers/projects.
- Counts by status when available.
- Shortcuts to Containers and Projects.

States:

- Loading skeleton.
- Empty/no resources.
- Environment offline.
- Unauthorized/missing permission.
- Server unreachable.

## Containers list

Purpose:

- Let users scan container state quickly and choose one for details/actions.

Content:

- Container name.
- Status/state.
- Image.
- Project/compose association when available.
- Ports or primary published ports if compact enough.
- Search/filter by name/status if simple.

Actions:

- Pull-to-refresh.
- Tap row for detail.
- Optional contextual start/stop/restart if the row UI can avoid accidental taps.

Acceptance criteria:

- The list is useful on a small screen.
- Status is visually obvious but not color-only.
- Empty and permission-denied states are explicit.

## Container detail

Purpose:

- Show enough detail to make safe lifecycle decisions.

Content:

- Name, ID short form, status.
- Image.
- Created/started time if available.
- Ports.
- Labels/project association where useful.
- Basic resource stats if available without overcomplicating MVP.

Safe actions:

- Start.
- Stop.
- Restart.

Confirmation behavior:

- Start may be immediate if the container is stopped.
- Stop and restart require confirmation.
- Show a pending/progress state while the action runs.
- Refresh detail/list state after action completion.

Destructive/non-MVP actions:

- Delete/remove/redeploy/update are hidden or disabled in MVP.

## Projects list

Purpose:

- Show Arcane-managed Compose projects as app-level units.

Content:

- Project name.
- Status/deployment state.
- Environment.
- Running/stopped service summary when available.
- Update indicator if available.

Actions:

- Pull-to-refresh.
- Tap for detail.

## Project detail

Purpose:

- Show project status and expose safe lifecycle controls.

Content:

- Project name and status.
- Runtime summary.
- Services/containers summary.
- Updates if available.

Safe actions:

- Up/deploy.
- Down.
- Restart.
- Pull images if treated as safe enough for MVP; otherwise defer.

Confirmation behavior:

- Down and restart require confirmation.
- Up/deploy should show clear pending state.
- If an action starts a background operation, show accepted/running state and refresh until stable.

Non-MVP actions:

- Create project.
- Edit compose/includes.
- Destroy project.
- Build images.
- Archive/unarchive.

## Settings

MVP settings:

- Current server URL.
- Current account identity.
- Current environment.
- Switch environment.
- Log out.
- Clear local app data for the server.
- App version/build info.

Optional developer settings:

- Allow self-signed certificates after explicit warning.
- Toggle API key mode.
- Debug API base URL display.

## Error and offline states

Required error categories:

- Invalid server URL.
- Server unreachable.
- TLS/certificate failure.
- Not an Arcane server / unexpected response.
- Unauthenticated / expired auth.
- Unauthorized / missing permission.
- Environment unavailable.
- Action failed.
- Empty resource list.

Guidance:

- Make retry actions obvious.
- Avoid losing user-entered server URL or credentials on transient failures.
- For action failures, keep the user on the current screen and show the failed action and target resource.

## Safety policy

MVP includes only low-to-medium risk operational actions:

- Container start/stop/restart.
- Project up/down/restart.

MVP excludes destructive actions:

- Delete container.
- Remove image/volume/network.
- Prune resources.
- Destroy project.
- Delete environment.

All stop/down/restart actions need confirmation. Confirmation copy should name the exact container or project.

## Android implementation assumptions

- Native Android using Kotlin and Jetpack Compose.
- MVVM-ish architecture with repositories/services for API calls.
- One central Arcane API client.
- Secure local storage for auth material.
- Coroutine-based networking.
- StateFlow or equivalent observable state for UI.
- UI should remain functional with one server first; multi-server support can be modeled but does not need complex account switching in MVP.

## Acceptance checklist

MVP is acceptable when a human can manually verify:

- Add a server URL.
- Authenticate.
- See current user identity or at least successfully validate session.
- Select or default to environment `0`.
- See dashboard/health summary.
- List containers.
- Open a container detail screen.
- Start/stop/restart a test container and see state refresh.
- List projects.
- Open a project detail screen.
- Run at least one safe project lifecycle action against a test project, if available.
- Log out and confirm local auth is cleared.
- See useful errors when the server is offline or auth expires.

## Future backlog

Post-MVP candidates:

- Read-only logs.
- WebSocket live stats.
- Images/volumes/networks read-only screens.
- Activity/jobs screen.
- Notifications.
- Project update/pull/build workflows.
- Container update/redeploy workflows.
- Admin/user management.
- Swarm views.
- Multi-server switching.
- Tablet layout.

## AI disclosure note

This spec was drafted with Hermes Agent assistance and should be human-reviewed/edited before submission upstream, per `AI_POLICY.md`.
