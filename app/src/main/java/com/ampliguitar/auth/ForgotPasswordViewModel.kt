package com.ampliguitar.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ampliguitar.data.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class ForgotPasswordState {
    object Idle : ForgotPasswordState()
    object Loading : ForgotPasswordState()
    data class EmailVerified(val email: String) : ForgotPasswordState()
    data class Success(val message: String) : ForgotPasswordState()
    data class Error(val message: String) : ForgotPasswordState()
}

class ForgotPasswordViewModel : ViewModel() {
    private val authRepository = AuthRepository

    private val _forgotPasswordState = MutableStateFlow<ForgotPasswordState>(ForgotPasswordState.Idle)
    val forgotPasswordState = _forgotPasswordState.asStateFlow()

    fun verifyEmail(email: String) {
        viewModelScope.launch {
            _forgotPasswordState.value = ForgotPasswordState.Loading
            if (email.isBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                _forgotPasswordState.value = ForgotPasswordState.Error("Masukkan email yang valid.")
                return@launch
            }

            val user = authRepository.findUserByEmail(email)
            if (user != null) {
                _forgotPasswordState.value = ForgotPasswordState.EmailVerified(email)
            } else {
                _forgotPasswordState.value = ForgotPasswordState.Error("Email tidak ditemukan di database.")
            }
        }
    }

    fun resetPasswordManual(email: String, newPassword: String) {
        viewModelScope.launch {
            _forgotPasswordState.value = ForgotPasswordState.Loading
            if (newPassword.length < 6) {
                _forgotPasswordState.value = ForgotPasswordState.Error("Password minimal 6 karakter.")
                return@launch
            }

            val result = authRepository.resetPasswordManual(email, newPassword)
            _forgotPasswordState.value = if (result.isSuccess) {
                ForgotPasswordState.Success("Password berhasil diubah secara manual! Silakan login.")
            } else {
                ForgotPasswordState.Error(result.exceptionOrNull()?.message ?: "Gagal mengubah password.")
            }
        }
    }

    fun resetState() {
        _forgotPasswordState.value = ForgotPasswordState.Idle
    }
}
