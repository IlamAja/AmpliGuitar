package com.ampliguitar.profile

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ampliguitar.ui.theme.AmpliGuitarTheme

class ProfileActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AmpliGuitarTheme {
                ProfileScreen(onBack = { finish() })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(onBack: () -> Unit, viewModel: ProfileViewModel = viewModel()) {
    val currentUser by viewModel.currentUser.collectAsState()
    val updateState by viewModel.updateState.collectAsState()

    var name by remember { mutableStateOf("") }
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    val context = LocalContext.current

    // Update name field when user data is loaded
    LaunchedEffect(currentUser) {
        currentUser?.let { name = it.name }
    }

    // Show feedback messages
    LaunchedEffect(updateState) {
        when (val state = updateState) {
            is ProfileUpdateState.Success -> {
                Toast.makeText(context, state.message, Toast.LENGTH_SHORT).show()
                viewModel.resetState()
            }
            is ProfileUpdateState.Error -> {
                Toast.makeText(context, state.message, Toast.LENGTH_SHORT).show()
                viewModel.resetState()
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profil Saya") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") } }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Form Ubah Nama
            Text("Ubah Nama", style = MaterialTheme.typography.titleLarge)
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nama") },
                modifier = Modifier.fillMaxWidth()
            )
            Button(
                onClick = { viewModel.updateName(name) },
                enabled = updateState !is ProfileUpdateState.Loading,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Simpan Nama")
            }

            HorizontalDivider()

            // Form Ubah Password
            Text("Ubah Password", style = MaterialTheme.typography.titleLarge)
            OutlinedTextField(
                value = currentPassword,
                onValueChange = { currentPassword = it },
                label = { Text("Password Saat Ini") },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = newPassword,
                onValueChange = { newPassword = it },
                label = { Text("Password Baru") },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = { Text("Konfirmasi Password Baru") },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth()
            )
            Button(
                onClick = { viewModel.updatePassword(currentPassword, newPassword, confirmPassword) },
                enabled = updateState !is ProfileUpdateState.Loading,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Simpan Password")
            }
        }
    }
}