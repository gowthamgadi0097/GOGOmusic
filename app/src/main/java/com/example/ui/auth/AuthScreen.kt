package com.example.ui.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
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
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.GoogleLogoIcon
import com.example.ui.components.GoogleSignInDialog
import com.example.ui.theme.CardBorder
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonViolet
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun AuthScreen(
    viewModel: AuthViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF130924),
                        DarkBackground,
                        Color(0xFF09131C)
                    )
                )
            )
            .imePadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Vault Emblem with glowing badge
            Box(
                modifier = Modifier
                    .size(88.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                NeonViolet.copy(alpha = 0.35f),
                                Color.Transparent
                            )
                        )
                    )
                    .border(2.dp, Brush.linearGradient(listOf(NeonViolet, NeonCyan)), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(DarkSurfaceElevated),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.MusicNote,
                        contentDescription = "SoundVault Logo",
                        tint = NeonCyan,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "SoundVault",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 32.sp,
                    brush = Brush.horizontalGradient(listOf(NeonViolet, NeonCyan))
                ),
                textAlign = TextAlign.Center
            )

            Text(
                text = "Secure Private Music Storage & Player",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Card container for auth form
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .border(1.dp, CardBorder, RoundedCornerShape(24.dp)),
                color = DarkSurface.copy(alpha = 0.9f)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Mode Tabs: Log In vs Create Account
                    TabRow(
                        selectedTabIndex = if (state.isLoginMode) 0 else 1,
                        containerColor = DarkSurfaceElevated,
                        contentColor = TextPrimary,
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .height(48.dp),
                        indicator = { tabPositions ->
                            val currentTab = if (state.isLoginMode) 0 else 1
                            TabRowDefaults.SecondaryIndicator(
                                modifier = Modifier.tabIndicatorOffset(tabPositions[currentTab]),
                                color = NeonViolet,
                                height = 3.dp
                            )
                        },
                        divider = {}
                    ) {
                        Tab(
                            selected = state.isLoginMode,
                            onClick = { if (!state.isLoginMode) viewModel.toggleMode() },
                            text = {
                                Text(
                                    text = "Log In",
                                    fontWeight = if (state.isLoginMode) FontWeight.Bold else FontWeight.Normal,
                                    color = if (state.isLoginMode) NeonViolet else TextSecondary
                                )
                            },
                            modifier = Modifier.testTag("auth_tab_login")
                        )
                        Tab(
                            selected = !state.isLoginMode,
                            onClick = { if (state.isLoginMode) viewModel.toggleMode() },
                            text = {
                                Text(
                                    text = "Sign Up",
                                    fontWeight = if (!state.isLoginMode) FontWeight.Bold else FontWeight.Normal,
                                    color = if (!state.isLoginMode) NeonCyan else TextSecondary
                                )
                            },
                            modifier = Modifier.testTag("auth_tab_signup")
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Error message banner
                    AnimatedVisibility(
                        visible = state.errorMessage != null,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp)
                                .clip(RoundedCornerShape(10.dp)),
                            color = MaterialTheme.colorScheme.error.copy(alpha = 0.15f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f))
                        ) {
                            Text(
                                text = state.errorMessage ?: "",
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(12.dp),
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    // Google Sign-In Button
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .border(1.dp, CardBorder, RoundedCornerShape(12.dp))
                            .clickable(enabled = !state.isLoading && !state.isGoogleLoading) {
                                viewModel.setShowGoogleAccountPicker(true)
                            }
                            .testTag("google_login_button"),
                        color = Color.White
                    ) {
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            GoogleLogoIcon(size = 20.dp)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = if (state.isLoginMode) "Sign in with Google" else "Sign up with Google",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1F1F1F)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // "OR" Divider
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        HorizontalDivider(
                            modifier = Modifier.weight(1f),
                            color = CardBorder
                        )
                        Text(
                            text = if (state.isLoginMode) "OR USE VAULT ID" else "OR REGISTER WITH EMAIL",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextMuted,
                            modifier = Modifier.padding(horizontal = 10.dp)
                        )
                        HorizontalDivider(
                            modifier = Modifier.weight(1f),
                            color = CardBorder
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Username Input
                    OutlinedTextField(
                        value = state.username,
                        onValueChange = { viewModel.onUsernameChange(it) },
                        label = { Text("Username") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Username",
                                tint = NeonViolet
                            )
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            imeAction = if (!state.isLoginMode) ImeAction.Next else ImeAction.Next
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonViolet,
                            unfocusedBorderColor = CardBorder,
                            focusedContainerColor = DarkSurfaceElevated,
                            unfocusedContainerColor = DarkSurfaceElevated,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedLabelColor = NeonViolet,
                            unfocusedLabelColor = TextSecondary
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("auth_username_input")
                    )

                    // Email Input (Only for Register)
                    AnimatedVisibility(visible = !state.isLoginMode) {
                        Column {
                            Spacer(modifier = Modifier.height(12.dp))
                            OutlinedTextField(
                                value = state.email,
                                onValueChange = { viewModel.onEmailChange(it) },
                                label = { Text("Email Address") },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Email,
                                        contentDescription = "Email",
                                        tint = NeonCyan
                                    )
                                },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Email,
                                    imeAction = ImeAction.Next
                                ),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = NeonCyan,
                                    unfocusedBorderColor = CardBorder,
                                    focusedContainerColor = DarkSurfaceElevated,
                                    unfocusedContainerColor = DarkSurfaceElevated,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary,
                                    focusedLabelColor = NeonCyan,
                                    unfocusedLabelColor = TextSecondary
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("auth_email_input")
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Password Input
                    OutlinedTextField(
                        value = state.password,
                        onValueChange = { viewModel.onPasswordChange(it) },
                        label = { Text("Password") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Password",
                                tint = NeonViolet
                            )
                        },
                        trailingIcon = {
                            IconButton(
                                onClick = { viewModel.togglePasswordVisibility() },
                                modifier = Modifier.size(48.dp)
                            ) {
                                Icon(
                                    imageVector = if (state.isPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = "Toggle password visibility",
                                    tint = TextSecondary
                                )
                            }
                        },
                        visualTransformation = if (state.isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = { viewModel.submit() }
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonViolet,
                            unfocusedBorderColor = CardBorder,
                            focusedContainerColor = DarkSurfaceElevated,
                            unfocusedContainerColor = DarkSurfaceElevated,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedLabelColor = NeonViolet,
                            unfocusedLabelColor = TextSecondary
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("auth_password_input")
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Submit Button
                    Button(
                        onClick = { viewModel.submit() },
                        enabled = !state.isLoading,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = NeonViolet,
                            contentColor = Color.White
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("auth_submit_button")
                    ) {
                        if (state.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = if (state.isLoginMode) "Log In to Vault" else "Create Secure Vault",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Quick Guest Demo Access Button
                    OutlinedButton(
                        onClick = { viewModel.loginAsGuest() },
                        enabled = !state.isLoading,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = NeonCyan
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, NeonCyan.copy(alpha = 0.6f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("auth_guest_button")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Quick Enter as Guest",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Security reassurance badge
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = null,
                    tint = TextMuted,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Encrypted Local Storage • Zero External Cloud Upload",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        // Google Sign-In Account Chooser Dialog
        if (state.showGoogleAccountPicker) {
            GoogleSignInDialog(
                onDismiss = { viewModel.setShowGoogleAccountPicker(false) },
                onConfirmSignIn = { email, displayName ->
                    viewModel.loginWithGoogle(email, displayName)
                },
                isLoading = state.isGoogleLoading
            )
        }
    }
}
