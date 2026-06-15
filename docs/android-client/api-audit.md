# Android Client API Audit

## Status

Source-derived audit for an Android MVP. This document consolidates the completed Kanban API audit work into the repository so it is not lost with scratch workspaces.

- Repository inspected: `mkaltner/arcane` on branch `feat/android-client-mvp`, based on upstream `origin/main` at `42a861d0`.
- API implementation: Go backend using Echo + Huma v2 under the `/api` group.
- Parsed Huma REST operations from source: 282.
- OpenAPI generation: supported by source code, but not run here because the local environment does not have `go` installed.
- Running server docs/OpenAPI locations:
  - `/api/docs`
  - `/api/openapi.json`

## Client connection model

The Android app should connect to an Arcane Manager, not directly to direct/edge agents in normal use.

Base URL construction:

```text
user-entered manager origin + /api
```

Examples:

```text
https://arcane.example.com/api
http://192.168.1.10:3552/api
```

WebSocket URLs should be derived from the same manager origin:

```text
https://host -> wss://host/api/...
http://host  -> ws://host/api/...
```

## Authentication

Arcane exposes two API security schemes in `backend/api/api.go`:

- `BearerAuth` — bearer JWT in the HTTP `Authorization` header.
- `ApiKeyAuth` — API key in the `X-Api-Key` header.

The auth middleware extracts a bearer token from the `Authorization` header first and can fall back to browser cookies. Android should not depend on browser cookie behavior. Preferred Android auth options:

1. Use local-auth login/refresh/current-user endpoints and send bearer auth explicitly.
2. Support manual API-key entry as an advanced/power-user path.

MVP auth endpoints:

- `POST /auth/login` — log in.
- `POST /auth/logout` — log out.
- `GET /auth/me` — fetch current user/session identity.
- `POST /auth/refresh` — refresh session token.
- `GET /auth/me/api-keys` — list current user's API keys.
- `POST /auth/me/api-keys` — create an API key.
- `DELETE /auth/me/api-keys/{id}` — delete an API key.

## Environment selection

Arcane models each managed Docker host as a Docker environment.

- Local Docker environment ID: `0`.
- Remote environments are discovered through the environment API.
- Docker-resource API calls include the environment ID in the path.

MVP environment endpoints:

- `GET /environments` — list environments.
- `GET /environments/{id}` — fetch one environment.
- `POST /environments/{id}/test` — test an environment connection.
- `GET /environments/{id}/version` — fetch environment version.
- `GET /environments/{id}/system/health` — environment/system health.
- `GET /environments/{id}/dashboard` — dashboard snapshot.

Android MVP behavior:

- Default to local environment `0` when only one environment exists.
- If multiple environments exist, add an environment picker before resource lists.
- Persist last selected server and environment.
- Treat connection failure, auth failure, and permission failure as distinct UI states.

## Containers

Container management is the highest-value first vertical slice.

MVP REST endpoints:

- `GET /environments/{id}/containers` — list containers.
- `GET /environments/{id}/containers/counts` — status counts.
- `GET /environments/{id}/containers/{containerId}` — container detail.
- `POST /environments/{id}/containers/{containerId}/start` — start container.
- `POST /environments/{id}/containers/{containerId}/stop` — stop container.
- `POST /environments/{id}/containers/{containerId}/restart` — restart container.

Non-MVP / defer initially:

- `POST /environments/{id}/containers` — create container.
- `DELETE /environments/{id}/containers/{containerId}` — delete container.
- `POST /environments/{id}/containers/{containerId}/redeploy` — redeploy container.
- `POST /environments/{id}/containers/{containerId}/auto-update` — set auto-update.
- `POST /environments/{id}/containers/{containerId}/update` — updater action.

WebSocket endpoints relevant to containers:

- `GET /api/environments/{id}/ws/containers/{containerId}/logs`
- `GET /api/environments/{id}/ws/containers/{containerId}/stats`
- `GET /api/environments/{id}/ws/containers/{containerId}/terminal`

Android MVP recommendation:

- Include list, detail, and start/stop/restart.
- Make logs read-only if included; otherwise defer logs to post-MVP.
- Defer terminal/exec entirely for the MVP because it is interactive and higher risk.

## Projects / Compose apps

Arcane's Compose app concept maps to Projects. Projects are a strong MVP candidate after containers because they represent user-facing applications.

MVP REST endpoints:

- `GET /environments/{id}/projects` — list projects.
- `GET /environments/{id}/projects/counts` — status counts.
- `GET /environments/{id}/projects/{projectId}` — project detail.
- `GET /environments/{id}/projects/{projectId}/runtime` — runtime state.
- `GET /environments/{id}/projects/{projectId}/updates` — update info.
- `POST /environments/{id}/projects/{projectId}/up` — deploy/start project.
- `POST /environments/{id}/projects/{projectId}/down` — bring project down.
- `POST /environments/{id}/projects/{projectId}/restart` — restart project.
- `POST /environments/{id}/projects/{projectId}/pull` — pull project images.

Defer initially:

- Creating/editing projects.
- Destroying projects.
- Compose file editing.
- Include-file editing.
- Build workspace and file-browser operations.

WebSocket endpoint:

- `GET /api/environments/{id}/ws/projects/{projectId}/logs`

## Images, volumes, networks, and system

These are useful but should be mostly read-only or deferred in the first app version.

Images:

- `GET /environments/{id}/images`
- `GET /environments/{id}/images/counts`
- `GET /environments/{id}/images/{imageId}`

Volumes:

- `GET /environments/{id}/volumes`
- `GET /environments/{id}/volumes/counts`
- `GET /environments/{id}/volumes/{volumeName}`

Networks:

- `GET /environments/{id}/networks`
- `GET /environments/{id}/networks/counts`
- `GET /environments/{id}/networks/{networkId}`
- `GET /environments/{id}/networks/topology`

System:

- `GET /environments/{id}/system/docker/info`
- `GET /environments/{id}/system/health`
- `GET /api/environments/{id}/ws/system/stats`

MVP recommendation:

- Include system health / dashboard summary if it helps the home screen.
- Defer destructive actions such as prune/delete/remove.
- Defer volume file browsing and backups.

## Response and client behavior notes

The frontend uses a shared API service with `/api` as the base path, auth refresh behavior on unauthorized responses, and environment-aware Docker resource calls. Android should mirror the same high-level behavior:

- Central API client owns base URL, auth headers, refresh/re-auth flow, JSON parsing, and error mapping.
- Environment-aware services take or resolve the selected environment ID before constructing resource paths.
- UI should distinguish:
  - server unreachable,
  - TLS/certificate failure,
  - unauthenticated / expired session,
  - unauthorized / missing permission,
  - environment offline,
  - empty resource list,
  - action accepted but resource state not yet updated.

## MVP API surface summary

Minimum useful Android API surface:

1. Server validation:
   - `GET /api/health` or `GET /api/environments/{id}/system/health` once authenticated.
2. Auth:
   - `POST /auth/login`
   - `GET /auth/me`
   - `POST /auth/refresh`
   - `POST /auth/logout`
3. Environment:
   - `GET /environments`
   - `GET /environments/{id}`
4. Dashboard:
   - `GET /environments/{id}/dashboard`
5. Containers:
   - `GET /environments/{id}/containers`
   - `GET /environments/{id}/containers/{containerId}`
   - `POST /environments/{id}/containers/{containerId}/start`
   - `POST /environments/{id}/containers/{containerId}/stop`
   - `POST /environments/{id}/containers/{containerId}/restart`
6. Projects:
   - `GET /environments/{id}/projects`
   - `GET /environments/{id}/projects/{projectId}`
   - `GET /environments/{id}/projects/{projectId}/runtime`
   - `POST /environments/{id}/projects/{projectId}/up`
   - `POST /environments/{id}/projects/{projectId}/down`
   - `POST /environments/{id}/projects/{projectId}/restart`

## Open questions

- Confirm exact Android auth storage policy: encrypted local token storage vs API key-only first version.
- Confirm whether self-signed TLS certificates should be supported for homelab managers, and if so what UX is acceptable.
- Confirm whether logs are part of MVP or first post-MVP release.
- Confirm package namespace and publication target before project scaffolding.

## AI disclosure note

This audit was drafted with Hermes Agent assistance and should be human-reviewed/edited before submission upstream, per `AI_POLICY.md`.
