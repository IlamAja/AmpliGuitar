package com.ampliguitar.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ampliguitar.data.AuthRepository
import com.ampliguitar.model.User
import com.ampliguitar.model.UserRole
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class UserManagementViewModel : ViewModel() {
    // Perbaikan: Menggunakan singleton AuthRepository secara langsung
    private val authRepository = AuthRepository

    val currentUser: StateFlow<User?> = authRepository.currentUser

    val regularUsers: StateFlow<List<User>> = authRepository.allUsers
        .map { userList ->
            userList.filter { it.role == UserRole.USER }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun deleteUser(userId: String) {
        viewModelScope.launch {
            authRepository.deleteUser(userId)
        }
    }
}