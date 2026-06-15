package app.arcane.android.ui.connect

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import app.arcane.android.ui.theme.ArcaneTheme

@Composable
fun ConnectRoute(
    viewModel: ConnectViewModel = hiltViewModel(),
) {
    ConnectScreen(
        formState = viewModel.formState,
        onServerUrlChanged = viewModel::onServerUrlChanged,
        onContinue = viewModel::continueToServer,
    )
}

@Composable
fun ConnectScreen(
    formState: ConnectFormState,
    onServerUrlChanged: (String) -> Unit,
    onContinue: () -> Unit,
) {
    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(PaddingValues(horizontal = 24.dp, vertical = 32.dp)),
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = "Connect to Arcane",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Enter your Arcane Manager URL to start the Android client setup.",
                style = MaterialTheme.typography.bodyLarge,
            )
            Spacer(modifier = Modifier.height(24.dp))
            OutlinedTextField(
                value = formState.rawServerUrl,
                onValueChange = onServerUrlChanged,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Manager URL") },
                placeholder = { Text("https://arcane.example.com") },
                singleLine = true,
                isError = formState.validationMessage != null && formState.rawServerUrl.isNotBlank(),
                supportingText = {
                    val message = formState.normalizedOrigin?.let { "Will connect to $it" }
                        ?: formState.validationMessage
                    if (message != null) Text(message)
                },
            )
            Spacer(modifier = Modifier.height(20.dp))
            Button(
                onClick = onContinue,
                enabled = formState.canContinue,
                modifier = Modifier.fillMaxWidth(),
            ) {
                if (formState.isSaving) {
                    CircularProgressIndicator()
                } else {
                    Text("Continue")
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ConnectScreenPreview() {
    ArcaneTheme {
        ConnectScreen(
            formState = ConnectFormState(rawServerUrl = "https://arcane.example.com"),
            onServerUrlChanged = {},
            onContinue = {},
        )
    }
}
