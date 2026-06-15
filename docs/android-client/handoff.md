# Android Client Handoff

## Branch

`feat/android-client-mvp`

## Workspace

`/home/nameless1/projects/arcane`

## Documentation home

`docs/android-client/`

## Artifact checklist

- [x] API audit committed to `docs/android-client/api-audit.md`
- [x] MVP product spec committed to `docs/android-client/mvp-spec.md`
- [x] Android implementation plan committed to `docs/android-client/implementation-plan.md`
- [x] Validation and follow-up notes committed to `docs/android-client/handoff.md`

## Current status

The Android workstream is now anchored in the repository instead of only in Hermes/Kanban scratch workspaces.

Completed repo artifacts:

- `README.md` — workstream overview and contribution/AI disclosure reminder.
- `api-audit.md` — source-derived Arcane API surface for an Android MVP.
- `mvp-spec.md` — first-version user flows, screens, safety policy, and acceptance criteria.
- `implementation-plan.md` — recommended Android stack, project layout, milestones, and validation plan.
- `handoff.md` — this file.
- `../../clients/android/` — Kotlin + Jetpack Compose Android scaffold.

## Key decisions captured

- Android app should connect to Arcane Manager, not directly to direct/edge agents in normal use.
- REST base URL is user-supplied manager origin plus `/api`.
- Local Docker environment ID is `0`.
- Docker resource calls should include selected environment ID.
- Android should prefer explicit bearer/API-key auth rather than browser-cookie assumptions.
- First implementation directory recommendation: `clients/android/`.
- Tentative Android namespace: `app.arcane.android`.
- Recommended stack: Kotlin + Jetpack Compose.
- MVP includes safe lifecycle actions only; destructive actions are deferred.

## Validation notes

Docs and Android scaffold verification:

- Markdown was manually reviewed at write time for secrets and malformed token placeholders.
- OpenAPI generation was not run because this machine does not currently have `go` installed.
- API audit is source-derived from Arcane backend/frontend files and parsed Huma route registrations.
- Local Android SDK command-line tools were installed under `$HOME/Android/Sdk` for verification.
- `clients/android/` was verified with:

  ```bash
  ./gradlew clean assembleDebug
  ./gradlew testDebugUnitTest lintDebug
  ```

- Both Gradle commands completed successfully. Unit tests were `NO-SOURCE` because no test files exist yet.

Before submitting any Android code PR, follow Arcane `AI_POLICY.md`:

1. Start development environment:

   ```bash
   ./scripts/development/dev.sh start
   ```

2. Verify frontend:

   ```text
   http://localhost:3000
   ```

3. Verify backend:

   ```text
   http://localhost:3552
   ```

4. Manually test the specific Android flows on an emulator or physical device.
5. Run relevant Android tests/lint.
6. Include AI disclosure in PR body.

## Suggested next Kanban cleanup

The original root task was auto-decomposed twice, producing duplicate child tasks. Recommended canonical batch to keep:

- `t_5547a422` — Document Arcane API requirements for Android — done.
- `t_4bd3f6d1` — Define the Android MVP user flows — done.
- `t_4af5239d` — Scaffold the Android application project — blocked; should be unblocked with the implementation-plan decisions.
- `t_1c0f0ac2` — Implement the Arcane API client layer — todo.
- `t_ccd570f4` — Build the core Android MVP screens — todo.
- `t_66a0391f` — Validate the MVP and prepare handoff notes — todo.

Recommended duplicate/older batch to archive or close after confirming no unique work remains:

- `t_149949a3` — Audit Arcane API for Android MVP needs — done; incorporated into `api-audit.md`.
- `t_7de416b0` — Define the Android MVP user flows — done; incorporated into `mvp-spec.md`.
- `t_fdad1caf` — Create the Android project skeleton — blocked; duplicate of `t_4af5239d`.
- `t_80f1e0ff` — Implement the Arcane API client layer — todo; duplicate of `t_1c0f0ac2`.
- `t_d8a99c44` — Build the MVP Android screens and wiring — todo; duplicate of `t_ccd570f4`.
- `t_14c1a4f9` — Document local setup and next steps — todo; overlaps with `t_66a0391f` and this handoff.

## Suggested next implementation step

The Android scaffold now exists at `clients/android/` with namespace `app.arcane.android` and Kotlin + Jetpack Compose.

Next implementation step: replace scaffold placeholders with the connect/auth/environment vertical slice from `implementation-plan.md`:

1. Server URL normalization and persistence.
2. Auth repository for login/current-user/refresh/logout.
3. Secure auth storage.
4. Environment list/default selection.
5. Home health/dashboard screen backed by real Arcane API responses.

## Notes for agents

- Follow `AI_POLICY.md` and `AGENTS.md`.
- Do not submit generated code for environments that cannot be manually tested.
- Keep AI disclosure in PR body / handoff notes.
- Prefer concise, human-editable docs over verbose generated output.
- Completed Kanban artifacts should be copied from scratch workspaces into this repo before task completion.
- Do not include or preserve secrets, tokens, passwords, API keys, or connection strings.

## AI disclosure note

These handoff notes were drafted with Hermes Agent assistance and should be human-reviewed/edited before submission upstream, per `AI_POLICY.md`.
