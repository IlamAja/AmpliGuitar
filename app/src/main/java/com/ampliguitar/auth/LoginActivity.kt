package com.ampliguitar.auth

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.ampliguitar.admin.MainAdminActivity
import com.ampliguitar.customer.MainCustomerActivity
import com.ampliguitar.data.AuthRepository
import com.ampliguitar.model.UserRole
import com.ampliguitar.ui.theme.AmpliGuitarTheme
import kotlinx.coroutines.launch

class LoginActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AmpliGuitarTheme {
                LoginScreen()
            }
        }
    }
}

@Composable
fun LoginScreen() {
    val context = LocalContext.current
    val authRepository = AuthRepository
    val coroutineScope = rememberCoroutineScope()

    val currentUser by authRepository.currentUser.collectAsState()

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var showForgotPassword by remember { mutableStateOf(false) }

    // LaunchedEffect ini berjalan sekali saat LoginScreen pertama kali ditampilkan.
    // Ini adalah tempat yang aman untuk memeriksa sesi pengguna yang ada.
    LaunchedEffect(Unit) {
        authRepository.checkSession()
    }

    // LaunchedEffect ini akan berjalan setiap kali nilai 'currentUser' berubah.
    // Jika pengguna berhasil login (atau sesinya ditemukan), ini akan menangani navigasi.
    LaunchedEffect(currentUser) {
        if (currentUser != null) {
            val intent = when (currentUser?.role) {
                UserRole.ADMIN -> Intent(context, MainAdminActivity::class.java)
                UserRole.USER -> Intent(context, MainCustomerActivity::class.java)
                else -> null // Role tidak diketahui, tetap di halaman login
            }
            intent?.let {
                it.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                context.startActivity(it)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Login AmpliGuitar",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it; errorMessage = null; showForgotPassword = false },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(),
            isError = errorMessage != null
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it; errorMessage = null; showForgotPassword = false },
            label = { Text("Password") },
            modifier = Modifier.fillMaxWidth(),
            isError = errorMessage != null
        )

        if (errorMessage != null) {
            Text(
                text = errorMessage!!,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        if (showForgotPassword) {
            TextButton(
                onClick = { context.startActivity(Intent(context, ForgotPasswordActivity::class.java)) },
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Text("Lupa Password?")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                if (email.isBlank() || password.isBlank()) {
                    errorMessage = "Email dan password harus diisi"
                    return@Button
                }
                isLoading = true
                errorMessage = null
                coroutineScope.launch {
                    val result = authRepository.login(email, password)
                    if (result.isFailure) {
                        errorMessage = result.exceptionOrNull()?.message ?: "Email atau password salah."
                        showForgotPassword = true
                    }
                    isLoading = false
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
            } else {
                Text("Login")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(
            onClick = { context.startActivity(Intent(context, RegisterActivity::class.java)) }
        ) {
            Text("Belum punya akun? Daftar di sini")
        }
    }
}
