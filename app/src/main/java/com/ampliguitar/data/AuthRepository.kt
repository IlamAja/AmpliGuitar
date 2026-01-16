package com.ampliguitar.data

import android.util.Log
import com.ampliguitar.model.User
import com.ampliguitar.model.UserRole
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

object AuthRepository {

    private const val TAG = "AuthRepository"
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val repositoryScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser = _currentUser.asStateFlow()

    private val _allUsers = MutableStateFlow<List<User>>(emptyList())
    val allUsers = _allUsers.asStateFlow()

    suspend fun findUserByEmail(email: String): User? = withContext(Dispatchers.IO) {
        try {
            val snapshot = firestore.collection("users")
                .whereEqualTo("email", email)
                .get()
                .await()
            snapshot.toObjects(User::class.java).firstOrNull()
        } catch (e: Exception) {
            null
        }
    }

    suspend fun resetPasswordManual(email: String, newPassword: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val user = findUserByEmail(email) ?: return@withContext Result.failure(Exception("Email tidak ditemukan."))
            firestore.collection("users").document(user.id).update("password", newPassword).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun sendPasswordResetEmail(email: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            auth.sendPasswordResetEmail(email).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateUserName(userId: String, newName: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            firestore.collection("users").document(userId).update("name", newName).await()
            if (_currentUser.value?.id == userId) {
                _currentUser.value = _currentUser.value?.copy(name = newName)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateUserPassword(currentPassword: String, newPassword: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val user = auth.currentUser ?: return@withContext Result.failure(Exception("Pengguna tidak login."))
            val credential = EmailAuthProvider.getCredential(user.email!!, currentPassword)
            user.reauthenticate(credential).await()
            user.updatePassword(newPassword).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createDefaultAdminIfNeeded() {
        val adminEmail = "admin@ampliguitar.com"
        val adminPassword = "admin123"
        try {
            val adminQuery = firestore.collection("users")
                .whereEqualTo("role", UserRole.ADMIN.name)
                .limit(1)
                .get()
                .await()

            if (adminQuery.isEmpty) {
                try {
                    val authResult = auth.createUserWithEmailAndPassword(adminEmail, adminPassword).await()
                    val firebaseUser = authResult.user
                    if (firebaseUser != null) {
                        val adminUser = User(
                            id = firebaseUser.uid,
                            name = "Admin AmpliGuitar",
                            email = adminEmail,
                            role = UserRole.ADMIN,
                            password = adminPassword
                        )
                        firestore.collection("users").document(firebaseUser.uid).set(adminUser).await()
                        auth.signOut()
                    }
                } catch (e: FirebaseAuthUserCollisionException) {
                    // Already exists
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Gagal create admin", e)
        }
    }

    suspend fun checkSession() {
        checkCurrentUser()
        getAllUsers()
    }

    private suspend fun checkCurrentUser() {
        withContext(Dispatchers.IO) {
            val firebaseUser = auth.currentUser
            if (firebaseUser != null) {
                try {
                    val document = firestore.collection("users").document(firebaseUser.uid).get().await()
                    val user = document.toObject(User::class.java)
                    _currentUser.value = user
                } catch (e: Exception) {
                    _currentUser.value = null
                }
            } else {
                _currentUser.value = null
            }
        }
    }

    private fun getAllUsers() {
        firestore.collection("users").addSnapshotListener { snapshot, e ->
            if (e != null) return@addSnapshotListener
            if (snapshot != null) {
                repositoryScope.launch {
                    val userList = snapshot.toObjects(User::class.java)
                    _allUsers.value = userList
                }
            }
        }
    }

    suspend fun login(email: String, password: String): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                auth.signInWithEmailAndPassword(email, password).await()
                val firebaseUser = auth.currentUser
                if (firebaseUser != null) {
                    val document = firestore.collection("users").document(firebaseUser.uid).get().await()
                    val user = document.toObject(User::class.java)
                    if (user != null) {
                        _currentUser.value = user
                        return@withContext Result.success(Unit)
                    }
                }
            } catch (e: Exception) {
                Log.d(TAG, "Standard login failed")
            }

            try {
                val snapshot = firestore.collection("users")
                    .whereEqualTo("email", email)
                    .whereEqualTo("password", password)
                    .get()
                    .await()
                
                val user = snapshot.toObjects(User::class.java).firstOrNull()
                if (user != null) {
                    _currentUser.value = user
                    return@withContext Result.success(Unit)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Manual login failed", e)
            }

            Result.failure(Exception("Email atau password salah."))
        }
    }

    suspend fun register(name: String, email: String, password: String): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val authResult = auth.createUserWithEmailAndPassword(email, password).await()
                val firebaseUser = authResult.user
                if (firebaseUser != null) {
                    val user = User(
                        id = firebaseUser.uid,
                        name = name,
                        email = email,
                        role = UserRole.USER,
                        password = password
                    )
                    firestore.collection("users").document(firebaseUser.uid).set(user).await()
                    Result.success(Unit)
                } else {
                    Result.failure(Exception("Gagal membuat pengguna."))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun deleteUser(userId: String) = withContext(Dispatchers.IO) {
        try {
            firestore.collection("users").document(userId).delete().await()
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting user", e)
        }
    }

    fun logout() {
        auth.signOut()
        _currentUser.value = null
    }
}
