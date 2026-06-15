package app.arcane.android.di

import app.arcane.android.core.network.ServerUrl
import app.arcane.android.data.api.ArcaneAuthProvider
import app.arcane.android.data.api.ArcaneAuthState
import app.arcane.android.data.api.ArcaneJson
import app.arcane.android.data.api.ServerUrlProvider
import app.arcane.android.data.api.createArcaneHttpClient
import app.arcane.android.data.repository.DefaultArcaneRepository
import app.arcane.android.data.settings.SettingsDataStore
import app.arcane.android.domain.repository.ArcaneRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.ktor.client.HttpClient
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.Json
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    @Provides
    @Singleton
    fun provideJson(): Json = ArcaneJson.create()

    @Provides
    @Singleton
    fun provideHttpClient(json: Json): HttpClient = createArcaneHttpClient(json)

    @Provides
    @Singleton
    fun provideServerUrlProvider(settingsDataStore: SettingsDataStore): ServerUrlProvider = ServerUrlProvider {
        val origin = settingsDataStore.settings.first().serverOrigin
        ServerUrl.parse(requireNotNull(origin) { "Arcane server URL is not configured" })
    }

    @Provides
    @Singleton
    fun provideAuthProvider(settingsDataStore: SettingsDataStore): ArcaneAuthProvider = ArcaneAuthProvider {
        settingsDataStore.settings.first().authSession?.let { ArcaneAuthState.Bearer(it.token) }
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    @Singleton
    abstract fun bindArcaneRepository(repository: DefaultArcaneRepository): ArcaneRepository
}
