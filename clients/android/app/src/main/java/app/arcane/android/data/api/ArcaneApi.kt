package app.arcane.android.data.api

import app.arcane.android.domain.model.ArcaneStatus
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ArcaneApi @Inject constructor(
    private val httpClient: HttpClient,
) {
    suspend fun getStatus(): ArcaneStatus = httpClient.get("status").body()
}
