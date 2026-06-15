package app.arcane.android.ui.connect

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import app.arcane.android.ui.theme.ArcaneColors
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
    Scaffold(containerColor = ArcaneColors.Background) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(ArcaneColors.Background)
                .padding(innerPadding)
                .padding(PaddingValues(horizontal = 20.dp, vertical = 28.dp)),
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = ArcaneColors.Surface),
                border = BorderStroke(1.dp, ArcaneColors.Border),
            ) {
                Column(
                    modifier = Modifier.padding(22.dp),
                    verticalArrangement = Arrangement.Center,
                ) {
                    Text(
                        text = "ARCANE",
                        style = MaterialTheme.typography.labelLarge,
                        color = ArcaneColors.PrimaryPurple,
                        fontWeight = FontWeight.Black,
                        letterSpacing = MaterialTheme.typography.labelLarge.letterSpacing,
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "Connect to Arcane",
                        style = MaterialTheme.typography.headlineMedium,
                        color = ArcaneColors.TextPrimary,
                        fontWeight = FontWeight.Bold,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Enter your Arcane Manager URL to start the Android client setup.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = ArcaneColors.TextSecondary,
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    OutlinedTextField(
                        value = formState.rawServerUrl,
                        onValueChange = onServerUrlChanged,
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Manager URL") },
                        placeholder = { Text("https://arcane.example.com") },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = ArcaneColors.TextPrimary,
                            unfocusedTextColor = ArcaneColors.TextPrimary,
                            focusedContainerColor = ArcaneColors.SurfaceElevated,
                            unfocusedContainerColor = ArcaneColors.SurfaceElevated,
                            focusedBorderColor = ArcaneColors.PrimaryPurple,
                            unfocusedBorderColor = ArcaneColors.Border,
                            focusedLabelColor = ArcaneColors.PrimaryPurple,
                            unfocusedLabelColor = ArcaneColors.TextSecondary,
                            focusedPlaceholderColor = ArcaneColors.TextMuted,
                            unfocusedPlaceholderColor = ArcaneColors.TextMuted,
                            focusedSupportingTextColor = ArcaneColors.SuccessGreen,
                            unfocusedSupportingTextColor = ArcaneColors.TextSecondary,
                        ),
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
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ArcaneColors.PrimaryPurpleDark,
                            contentColor = ArcaneColors.TextPrimary,
                            disabledContainerColor = ArcaneColors.SurfaceMuted,
                            disabledContentColor = ArcaneColors.TextMuted,
                        ),
                    ) {
                        if (formState.isSaving) {
                            CircularProgressIndicator(color = ArcaneColors.TextPrimary)
                        } else {
                            Text("Continue")
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ConnectScreenPreview() {
    ArcaneTheme(darkTheme = true) {
        ConnectScreen(
            formState = ConnectFormState(rawServerUrl = "https://arcane.example.com"),
            onServerUrlChanged = {},
            onContinue = {},
        )
    }
}
