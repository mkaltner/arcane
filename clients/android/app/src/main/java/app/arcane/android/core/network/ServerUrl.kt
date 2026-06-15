package app.arcane.android.core.network

import java.net.URI

/**
 * Normalized Arcane Manager URL configuration.
 *
 * [origin] is the user-supplied Manager origin with whitespace and trailing slashes removed.
 * [apiBaseUrl] is the REST API base derived from that origin.
 */
data class ServerUrl(
    val origin: String,
    val apiBaseUrl: String,
) {
    companion object {
        fun parse(rawValue: String): ServerUrl {
            val trimmed = rawValue.trim()
            if (trimmed.isBlank()) {
                throw InvalidServerUrlException("Server URL is required")
            }

            val uri = runCatching { URI(trimmed) }
                .getOrElse { throw InvalidServerUrlException("Server URL is malformed", it) }

            val scheme = uri.scheme?.lowercase()
            if (scheme != "http" && scheme != "https") {
                throw InvalidServerUrlException("Server URL must start with http:// or https://")
            }

            if (uri.host.isNullOrBlank()) {
                throw InvalidServerUrlException("Server URL must include a host")
            }

            if (uri.rawQuery != null || uri.rawFragment != null) {
                throw InvalidServerUrlException("Server URL must not include a query string or fragment")
            }

            val normalizedPath = uri.rawPath
                ?.trimEnd('/')
                ?.withoutKnownArcaneRouteSuffix()
                .orEmpty()
            val authority = uri.rawAuthority ?: throw InvalidServerUrlException("Server URL must include a host")
            val origin = buildString {
                append(scheme)
                append("://")
                append(authority)
                append(normalizedPath)
            }.trimEnd('/')

            return ServerUrl(
                origin = origin,
                apiBaseUrl = "$origin/api",
            )
        }
    }
}

private fun String.withoutKnownArcaneRouteSuffix(): String {
    val routeSuffixes = listOf("/dashboard", "/login", "/api")
    return routeSuffixes.firstOrNull { suffix -> this == suffix || endsWith(suffix) }
        ?.let { suffix -> removeSuffix(suffix) }
        ?: this
}

class InvalidServerUrlException(
    message: String,
    cause: Throwable? = null,
) : IllegalArgumentException(message, cause)
