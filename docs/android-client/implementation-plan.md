# Android Client Implementation Plan

## Recommended project location

Use:

```text
clients/android/
```

Rationale:

- Keeps mobile clients separate from the existing web frontend/backend/CLI modules.
- Leaves room for future clients without overloading the repository root.
- Avoids mixing Android Gradle files into existing Go/Svelte workspaces.

## Recommended stack

- Language: Kotlin.
- UI: Jetpack Compose.
- Minimum SDK: decide during scaffold, but start with a modern baseline compatible with current Compose tooling.
- Networking: OkHttp + Retrofit or Ktor Client.
- JSON: kotlinx.serialization or Moshi.
- Async/state: Kotlin coroutines + Flow/StateFlow.
- Persistence:
  - EncryptedSharedPreferences / AndroidX Security for auth material.
  - DataStore for non-secret preferences such as server URL and selected environment.
- Dependency injection: Hilt or lightweight manual DI for MVP. Manual DI is acceptable for first scaffold if kept clean.

## Package namespace

Tentative namespace:

```text
app.arcane.android
```

Confirm before publishing or generating irreversible package metadata. If the upstream project has a preferred domain/package, use that instead.

## High-level modules

For an MVP single Android app module is enough:

```text
clients/android/
  settings.gradle.kts
  build.gradle.kts
  app/
    build.gradle.kts
    src/main/
      AndroidManifest.xml
      java/app/arcane/android/
        MainActivity.kt
        core/
          network/
          storage/
          model/
        data/
          auth/
          environment/
          container/
          project/
        ui/
          app/
          connect/
          login/
          home/
          containers/
          projects/
          settings/
```

If the app grows, split later into Gradle modules such as `core:network`, `core:model`, and `feature:*`. Do not over-module the initial scaffold.

## Architecture

Use a simple layered structure:

- `core/network`
  - Builds the HTTP client from current server/auth config.
  - Adds bearer/API-key headers.
  - Maps API errors into typed client errors.
  - Owns WebSocket client creation later.
- `core/storage`
  - Stores server URL and selected environment.
  - Stores auth material securely.
- `data/*`
  - API DTOs and repository classes by domain.
  - AuthRepository.
  - EnvironmentRepository.
  - ContainerRepository.
  - ProjectRepository.
- `ui/*`
  - Compose screens and ViewModels/state holders.
  - UI state models for loading/data/empty/error/action-pending.

## API client work breakdown

### 1. Core API client

Deliverables:

- Base URL model:
  - `serverOrigin`
  - `apiBaseUrl = serverOrigin + /api`
- Auth header provider:
  - bearer mode,
  - API-key mode,
  - unauthenticated mode.
- Standard error mapping:
  - network unreachable,
  - TLS/certificate failure,
  - HTTP 401,
  - HTTP 403,
  - HTTP 404,
  - server error,
  - unexpected payload.
- JSON parser setup.

Verification:

- Unit tests for URL normalization.
- Unit tests for auth header selection.
- Unit tests for error mapping with mocked responses.

### 2. Auth repository

Endpoints:

- `POST /auth/login`
- `GET /auth/me`
- `POST /auth/refresh`
- `POST /auth/logout`

Deliverables:

- Login request/response DTOs based on source/OpenAPI verification.
- Current user DTO.
- Token/session storage integration.
- Logout clears secure storage.

Verification:

- Manual login against a local Arcane Manager.
- Expired/invalid credentials show useful UI state.

### 3. Environment repository

Endpoints:

- `GET /environments`
- `GET /environments/{id}`
- `POST /environments/{id}/test`
- `GET /environments/{id}/system/health`
- `GET /environments/{id}/dashboard`

Deliverables:

- Environment list and selected environment state.
- Default selection logic for local environment `0`.
- Health/dashboard fetch.

Verification:

- Environment `0` is selected by default when available.
- Multi-environment response can be displayed and changed.

### 4. Container repository

Endpoints:

- `GET /environments/{id}/containers`
- `GET /environments/{id}/containers/{containerId}`
- `POST /environments/{id}/containers/{containerId}/start`
- `POST /environments/{id}/containers/{containerId}/stop`
- `POST /environments/{id}/containers/{containerId}/restart`

Deliverables:

- Container list DTO and UI summary model.
- Container detail DTO and UI detail model.
- Start/stop/restart actions.
- Action result refresh behavior.

Verification:

- List and detail load against local test server.
- Start/stop/restart work on a disposable test container.
- Action failure is visible and does not corrupt local UI state.

### 5. Project repository

Endpoints:

- `GET /environments/{id}/projects`
- `GET /environments/{id}/projects/{projectId}`
- `GET /environments/{id}/projects/{projectId}/runtime`
- `POST /environments/{id}/projects/{projectId}/up`
- `POST /environments/{id}/projects/{projectId}/down`
- `POST /environments/{id}/projects/{projectId}/restart`

Deliverables:

- Project list DTO and UI summary model.
- Project detail/runtime model.
- Up/down/restart actions.

Verification:

- List/detail load against local test server with at least one test project.
- Safe action confirmations name the exact project.

## UI work breakdown

### Connect screen

- Server URL text field.
- Validation and normalization.
- Continue button.
- Recent/last server if available.
- Clear network/TLS/error messages.

### Login screen

- Username/password login mode.
- Optional API key mode.
- Loading state.
- Auth failure state.
- Successful login navigates to environment/home.

### Environment picker

- Shows available environments.
- Highlights selected environment.
- Shows local environment `0` clearly.
- Allows switching from Settings and first-login flow.

### Home screen

- Server/environment header.
- Health/dashboard cards.
- Container/project status summaries.
- Navigation shortcuts.

### Containers screen

- List with pull-to-refresh.
- Search/filter if easy.
- Detail screen.
- Start/stop/restart confirmations.

### Projects screen

- List with pull-to-refresh.
- Detail/runtime screen.
- Up/down/restart confirmations.

### Settings screen

- Server URL display.
- Account display.
- Environment selection.
- Logout.
- Clear app data.
- App version.

## Validation plan

Before any PR containing Android code is marked ready, satisfy Arcane's `AI_POLICY.md` requirements:

1. Start Arcane development environment:

   ```bash
   ./scripts/development/dev.sh start
   ```

2. Verify frontend at:

   ```text
   http://localhost:3000
   ```

3. Verify backend at:

   ```text
   http://localhost:3552
   ```

4. Run Android unit tests.
5. Run Android app manually on an emulator or physical device.
6. Manually test the exact changed flows:
   - connect,
   - login,
   - environment selection,
   - container list/detail/action,
   - project list/detail/action,
   - logout.
7. Include AI disclosure in the PR body.

## Milestone plan

### Milestone 0 — docs and scaffold decision

- Commit API audit, MVP spec, implementation plan, handoff.
- Confirm package namespace and project directory.
- Confirm Android stack and minimum SDK.

### Milestone 1 — project scaffold

- Create `clients/android/` Gradle project.
- Add Compose app skeleton.
- Add basic navigation shell.
- Add CI-neutral README/setup notes.
- No Arcane API calls yet unless tooling is already verified.

### Milestone 2 — connect/auth/environment vertical slice

- Server URL setup.
- Auth repository.
- Secure auth storage.
- Environment list/default selection.
- Home health/dashboard screen.

### Milestone 3 — containers vertical slice

- Container list.
- Container detail.
- Start/stop/restart.
- Manual test against disposable container.

### Milestone 4 — projects vertical slice

- Project list.
- Project detail/runtime.
- Up/down/restart.
- Manual test against disposable project.

### Milestone 5 — polish and PR readiness

- Error states.
- Empty states.
- Logout/clear data.
- README.
- Tests.
- Human manual verification.
- Draft PR with AI disclosure.

## Immediate next implementation task

After docs are committed, unblock the Android scaffold ticket with this decision unless the human chooses otherwise:

- Directory: `clients/android/`
- Namespace: `app.arcane.android`
- Stack: Kotlin + Jetpack Compose
- First code milestone: empty app shell plus connect/login navigation placeholders

## AI disclosure note

This implementation plan was drafted with Hermes Agent assistance and should be human-reviewed/edited before submission upstream, per `AI_POLICY.md`.
