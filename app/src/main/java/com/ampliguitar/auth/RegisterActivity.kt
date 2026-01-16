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
import com.ampliguitar.data.AuthRepository
import com.ampliguitar.ui.theme.AmpliGuitarTheme
import kotlinx.coroutines.launch

class RegisterActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AmpliGuitarTheme {
                RegisterScreen()
            }
        }
    }
}

@Composable
fun RegisterScreen() {
    val context = LocalContext.current
    // Perbaikan: Menggunakan singleton AuthRepository secara langsung
    val authRepository = AuthRepository
    val coroutineScope = rememberCoroutineScope()

    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    // Perbaikan: Menghapus spasi pada mutableStateOf
    var successMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Daftar Akun Baru", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(value = name, onValueChange = { name = it; errorMessage = null }, label = { Text("Nama Lengkap") }, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(value = email, onValueChange = { email = it; errorMessage = null }, label = { Text("Email") }, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(value = password, onValueChange = { password = it; errorMessage = null }, label = { Text("Password (min. 6 karakter)") }, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(value = confirmPassword, onValueChange = { confirmPassword = it; errorMessage = null }, label = { Text("Konfirmasi Password") }, modifier = Modifier.fillMaxWidth())

        errorMessage?.let {
            Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 8.dp))
        }
        successMessage?.let {
            Text(it, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 8.dp))
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                if (name.isBlank() || email.isBlank() || password.isBlank()) {
                    errorMessage = "Semua kolom harus diisi."
                    return@Button
                }
                if (password.length < 6) {
                    errorMessage = "Password minimal 6 karakter."
                    return@Button
                }
                if (password != confirmPassword) {
                    errorMessage = "Password dan konfirmasi tidak cocok."
                    return@Button
                }

                isLoading = true
                errorMessage = null
                successMessage = null

                coroutineScope.launch {
                    val result = authRepository.register(name, email, password)
                    result.onSuccess {
                        successMessage = "Pendaftaran berhasil! Silakan kembali ke halaman login."
                        isLoading = false
                    }.onFailure {
                        errorMessage = it.message ?: "Terjadi kesalahan saat pendaftaran."
                        isLoading = false
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
            } else {
                Text("Daftar")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(onClick = { context.startActivity(Intent(context, LoginActivity::class.java)) }) {
            Text("Sudah punya akun? Login di sini")
        }
    }
}