package com.ampliguitar.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ampliguitar.data.AuthRepository
import com.ampliguitar.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// Sealed class untuk UI state
sealed class ProfileUpdateState {
    object Idle : ProfileUpdateState()
    object Loading : ProfileUpdateState()
    data class Success(val message: String) : ProfileUpdateState()
    data class Error(val message: String) : ProfileUpdateState()
}

class ProfileViewModel : ViewModel() {
    private val authRepository = AuthRepository

    val currentUser: StateFlow<User?> = authRepository.currentUser

    private val _updateState = MutableStateFlow<ProfileUpdateState>(ProfileUpdateState.Idle)
    val updateState = _updateState.asStateFlow()

    fun updateName(newName: String) {
        viewModelScope.launch {
            _updateState.value = ProfileUpdateState.Loading
            val userId = currentUser.value?.id
            if (userId == null) {
                _updateState.value = ProfileUpdateState.Error("User not found.")
                return@launch
            }
            if (newName.isBlank()) {
                _updateState.value = ProfileUpdateState.Error("Name cannot be empty.")
                return@launch
            }

            val result = authRepository.updateUserName(userId, newName)
            _updateState.value = if (result.isSuccess) {
                ProfileUpdateState.Success("Name updated successfully.")
            } else {
                ProfileUpdateState.Error(result.exceptionOrNull()?.message ?: "Failed to update name.")
            }
        }
    }

    fun updatePassword(currentPassword: String, newPassword: String, confirmPassword: String) {
        viewModelScope.launch {
            _updateState.value = ProfileUpdateState.Loading
            if (newPassword != confirmPassword) {
                _updateState.value = ProfileUpdateState.Error("New passwords do not match.")
                return@launch
            }
            if (newPassword.length < 6) {
                _updateState.value = ProfileUpdateState.Error("Password must be at least 6 characters.")
                return@launch
            }

            val result = authRepository.updateUserPassword(currentPassword, newPassword)
            _updateState.value = if (result.isSuccess) {
                ProfileUpdateState.Success("Password updated successfully.")
            } else {
                ProfileUpdateState.Error(result.exceptionOrNull()?.message ?: "Failed to update password. Check your current password.")
            }
        }
    }

    fun resetState() {
        _updateState.value = ProfileUpdateState.Idle
    }
}