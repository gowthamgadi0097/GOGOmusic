package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CardBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.DarkSurfaceHighlight
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonViolet
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun GoogleSignInDialog(
    onDismiss: () -> Unit,
    onConfirmSignIn: (email: String, displayName: String) -> Unit,
    isLoading: Boolean = false,
    defaultEmail: String = "gowthamgadi0097@gmail.com",
    defaultName: String = "Gowtham Gadi"
) {
    var isCustomAccount by remember { mutableStateOf(false) }
    var customEmail by remember { mutableStateOf("") }
    var customName by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = { if (!isLoading) onDismiss() },
        containerColor = DarkSurface,
        modifier = Modifier
            .border(1.dp, CardBorder, RoundedCornerShape(24.dp))
            .testTag("google_sign_in_dialog"),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    GoogleLogoIcon(size = 24.dp)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Sign in with Google",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = TextPrimary
                    )
                }

                IconButton(
                    onClick = onDismiss,
                    enabled = !isLoading,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Text(
                    text = "Choose an account to continue to SoundVault Music Player",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (!isCustomAccount) {
                    // Pre-configured Google Account Card
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .border(1.5.dp, NeonCyan.copy(alpha = 0.6f), RoundedCornerShape(16.dp))
                            .clickable {
                                if (!isLoading) {
                                    onConfirmSignIn(defaultEmail, defaultName)
                                }
                            }
                            .testTag("google_account_preset_item"),
                        color = DarkSurfaceElevated
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.linearGradient(listOf(Color(0xFF4285F4), Color(0xFF34A853)))
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = defaultName.firstOrNull()?.toString() ?: "G",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = defaultName,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = TextPrimary
                                )
                                Text(
                                    text = defaultEmail,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary
                                )
                            }

                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Active selection",
                                tint = NeonCyan,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Option to use another Google account
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .border(1.dp, CardBorder, RoundedCornerShape(14.dp))
                            .clickable { isCustomAccount = true }
                            .testTag("google_use_another_account_button"),
                        color = DarkSurfaceHighlight
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(DarkSurfaceElevated),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = null,
                                    tint = NeonCyan,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Use another Google Account",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                color = TextPrimary
                            )
                        }
                    }
                } else {
                    // Custom Google Account Inputs
                    OutlinedTextField(
                        value = customName,
                        onValueChange = { customName = it },
                        label = { Text("Display Name") },
                        placeholder = { Text("e.g. Alex Rivera") },
                        leadingIcon = {
                            Icon(Icons.Default.Person, contentDescription = null, tint = NeonViolet)
                        },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonViolet,
                            unfocusedBorderColor = CardBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedContainerColor = DarkSurfaceElevated,
                            unfocusedContainerColor = DarkSurfaceElevated
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("google_custom_name_input")
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = customEmail,
                        onValueChange = { customEmail = it },
                        label = { Text("Google Email") },
                        placeholder = { Text("username@gmail.com") },
                        leadingIcon = {
                            Icon(Icons.Default.Email, contentDescription = null, tint = NeonCyan)
                        },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonCyan,
                            unfocusedBorderColor = CardBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedContainerColor = DarkSurfaceElevated,
                            unfocusedContainerColor = DarkSurfaceElevated
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("google_custom_email_input")
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "← Back to saved accounts",
                        style = MaterialTheme.typography.bodySmall,
                        color = NeonCyan,
                        modifier = Modifier
                            .clickable { isCustomAccount = false }
                            .padding(4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = CardBorder)
                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "To continue, Google will share your name, email address, and language preference with SoundVault. All music tracks remain securely in your local encrypted vault.",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted,
                    lineHeight = 14.sp
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (isCustomAccount) {
                        if (customEmail.isNotBlank()) {
                            onConfirmSignIn(customEmail, customName.ifBlank { customEmail.substringBefore("@") })
                        }
                    } else {
                        onConfirmSignIn(defaultEmail, defaultName)
                    }
                },
                enabled = !isLoading && (!isCustomAccount || customEmail.contains("@")),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = Color(0xFF1F1F1F)
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("google_confirm_login_button")
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        color = Color(0xFF1F1F1F),
                        strokeWidth = 2.dp
                    )
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        GoogleLogoIcon(size = 18.dp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Continue with Google",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                enabled = !isLoading,
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
            ) {
                Text("Cancel", color = TextSecondary)
            }
        }
    )
}
