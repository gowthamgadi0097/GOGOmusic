package com.example.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.SessionManager
import com.example.data.SoundVaultRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AuthUiState(
    val isLoginMode: Boolean = true,
    val username: String = "",
    val email: String = "",
    val password: String = "",
    val isPasswordVisible: Boolean = false,
    val isLoading: Boolean = false,
    val isGoogleLoading: Boolean = false,
    val showGoogleAccountPicker: Boolean = false,
    val errorMessage: String? = null,
    val isSuccess: Boolean = false
)

class AuthViewModel(
    private val repository: SoundVaultRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun toggleMode() {
        _uiState.update {
            it.copy(
                isLoginMode = !it.isLoginMode,
                errorMessage = null
            )
        }
    }

    fun setShowGoogleAccountPicker(show: Boolean) {
        _uiState.update { it.copy(showGoogleAccountPicker = show, errorMessage = null) }
    }

    fun loginWithGoogle(email: String, displayName: String, subId: String? = null) {
        val trimmedEmail = email.trim()
        if (trimmedEmail.isBlank() || !trimmedEmail.contains("@")) {
            _uiState.update { it.copy(errorMessage = "Please enter a valid Google email address") }
            return
        }

        _uiState.update { it.copy(isGoogleLoading = true, isLoading = true, errorMessage = null, showGoogleAccountPicker = false) }

        viewModelScope.launch {
            val result = repository.authenticateOrRegisterGoogleUser(
                email = trimmedEmail,
                displayName = displayName.ifBlank { trimmedEmail.substringBefore("@") },
                googleSubId = subId
            )

            if (result.isSuccess) {
                val user = result.getOrThrow()
                sessionManager.setLoggedInUser(user.userId)
                _uiState.update { it.copy(isGoogleLoading = false, isLoading = false, isSuccess = true) }
            } else {
                _uiState.update {
                    it.copy(
                        isGoogleLoading = false,
                        isLoading = false,
                        errorMessage = result.exceptionOrNull()?.message ?: "Google login failed"
                    )
                }
            }
        }
    }

    fun onUsernameChange(value: String) {
        _uiState.update { it.copy(username = value, errorMessage = null) }
    }

    fun onEmailChange(value: String) {
        _uiState.update { it.copy(email = value, errorMessage = null) }
    }

    fun onPasswordChange(value: String) {
        _uiState.update { it.copy(password = value, errorMessage = null) }
    }

    fun togglePasswordVisibility() {
        _uiState.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun submit() {
        val state = _uiState.value
        if (state.username.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Please enter your username") }
            return
        }
        if (state.password.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Please enter your password") }
            return
        }

        _uiState.update { it.copy(isLoading = true, errorMessage = null) }

        viewModelScope.launch {
            if (state.isLoginMode) {
                val result = repository.authenticateUser(state.username, state.password)
                if (result.isSuccess) {
                    val user = result.getOrThrow()
                    sessionManager.setLoggedInUser(user.userId)
                    _uiState.update { it.copy(isLoading = false, isSuccess = true) }
                } else {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = result.exceptionOrNull()?.message ?: "Login failed"
                        )
                    }
                }
            } else {
                if (state.email.isBlank()) {
                    _uiState.update { it.copy(isLoading = false, errorMessage = "Please enter an email address") }
                    return@launch
                }
                val result = repository.registerUser(state.username, state.email, state.password)
                if (result.isSuccess) {
                    val user = result.getOrThrow()
                    sessionManager.setLoggedInUser(user.userId)
                    _uiState.update { it.copy(isLoading = false, isSuccess = true) }
                } else {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = result.exceptionOrNull()?.message ?: "Registration failed"
                        )
                    }
                }
            }
        }
    }

    fun loginAsGuest() {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            val guestUsername = "VaultMember"
            val guestEmail = "member@soundvault.local"
            val guestPassword = "vault_password_123"

            var authResult = repository.authenticateUser(guestUsername, guestPassword)
            if (authResult.isFailure) {
                // Register the default guest account
                val regResult = repository.registerUser(guestUsername, guestEmail, guestPassword)
                if (regResult.isSuccess) {
                    authResult = regResult
                }
            }

            if (authResult.isSuccess) {
                val user = authResult.getOrThrow()
                sessionManager.setLoggedInUser(user.userId)
                _uiState.update { it.copy(isLoading = false, isSuccess = true) }
            } else {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Quick login failed: ${authResult.exceptionOrNull()?.message}"
                    )
                }
            }
        }
    }
}
