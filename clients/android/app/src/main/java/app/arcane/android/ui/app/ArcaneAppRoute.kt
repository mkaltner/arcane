package app.arcane.android.ui.app

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.arcane.android.ui.auth.AuthRoute
import app.arcane.android.ui.connect.ConnectRoute
import app.arcane.android.ui.home.HomeRoute

@Composable
fun ArcaneAppRoute(
    viewModel: AppViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    when (val state = uiState) {
        AppUiState.Loading -> AppLoadingScreen()
        AppUiState.NeedsServer -> ConnectRoute()
        is AppUiState.NeedsAuthentication -> AuthRoute(serverOrigin = state.serverOrigin)
        is AppUiState.Authenticated -> HomeRoute()
    }
}

@Composable
private fun AppLoadingScreen() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        CircularProgressIndicator()
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "Loading Arcane…")
    }
}
