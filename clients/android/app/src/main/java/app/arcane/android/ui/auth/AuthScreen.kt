package app.arcane.android.ui.auth

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.arcane.android.ui.theme.ArcaneColors
import app.arcane.android.ui.theme.ArcaneTheme

@Composable
fun AuthRoute(
    serverOrigin: String,
    viewModel: AuthViewModel = hiltViewModel(),
) {
    val formState by viewModel.formState.collectAsStateWithLifecycle()
    AuthScreen(
        serverOrigin = serverOrigin,
        formState = formState,
        onUsernameChanged = viewModel::onUsernameChanged,
        onPasswordChanged = viewModel::onPasswordChanged,
        onSubmit = viewModel::submit,
    )
}

@Composable
fun AuthScreen(
    serverOrigin: String,
    formState: AuthFormState,
    onUsernameChanged: (String) -> Unit,
    onPasswordChanged: (String) -> Unit,
    onSubmit: () -> Unit,
) {
    Scaffold(containerColor = ArcaneColors.Background) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(ArcaneColors.Background)
                .padding(innerPadding)
                .padding(20.dp),
            verticalArrangement = Arrangement.Center,
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = ArcaneColors.Surface),
                border = BorderStroke(1.dp, ArcaneColors.Border),
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text(
                        text = "SIGN IN",
                        style = MaterialTheme.typography.labelMedium,
                        color = ArcaneColors.TextSecondary,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = "Authenticate to $serverOrigin",
                        style = MaterialTheme.typography.headlineSmall,
                        color = ArcaneColors.TextPrimary,
                        fontWeight = FontWeight.Bold,
                    )
                    OutlinedTextField(
                        value = formState.username,
                        onValueChange = onUsernameChanged,
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Username") },
                        singleLine = true,
                        enabled = !formState.isSubmitting,
                    )
                    OutlinedTextField(
                        value = formState.password,
                        onValueChange = onPasswordChanged,
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Password") },
                        singleLine = true,
                        enabled = !formState.isSubmitting,
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    )
                    val helper = formState.errorMessage ?: formState.validationMessage
                    if (helper != null) {
                        Text(text = helper, color = ArcaneColors.WarningAmber, style = MaterialTheme.typography.bodyMedium)
                    }
                    Button(
                        onClick = onSubmit,
                        enabled = formState.canSubmit,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        if (formState.isSubmitting) {
                            CircularProgressIndicator(modifier = Modifier.padding(end = 8.dp), color = ArcaneColors.TextPrimary)
                        }
                        Text("Sign in")
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AuthScreenPreview() {
    ArcaneTheme(darkTheme = true) {
        AuthScreen(
            serverOrigin = "https://arcane.example.com",
            formState = AuthFormState(username = "demo"),
            onUsernameChanged = {},
            onPasswordChanged = {},
            onSubmit = {},
        )
    }
}
