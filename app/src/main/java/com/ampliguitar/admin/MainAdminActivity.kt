
package com.ampliguitar.admin

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ampliguitar.auth.LoginActivity
import com.ampliguitar.data.AuthRepository
import com.ampliguitar.model.UserRole
import com.ampliguitar.profile.ProfileActivity
import com.ampliguitar.ui.theme.AmpliGuitarTheme

class MainAdminActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AmpliGuitarTheme {
                AdminDashboard()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboard(
    dashboardViewModel: DashboardViewModel = viewModel()
) {
    val context = LocalContext.current
    val authRepository = AuthRepository
    val currentUser by authRepository.currentUser.collectAsState()

    val totalProducts by dashboardViewModel.totalProducts.collectAsState()
    val newOrders by dashboardViewModel.newOrders.collectAsState()
    val totalCustomers by dashboardViewModel.totalCustomers.collectAsState()

    LaunchedEffect(currentUser) {
        if (currentUser == null || currentUser?.role != UserRole.ADMIN) {
            val intent = Intent(context, LoginActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Admin AmpliGuitar", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                actions = {
                    Text("Admin: ${currentUser?.name ?: "Unknown"}")
                    Spacer(modifier = Modifier.width(16.dp))
                    TextButton(onClick = {
                        authRepository.logout()
                        context.startActivity(Intent(context, LoginActivity::class.java))
                    }) {
                        Text("Logout", color = MaterialTheme.colorScheme.onPrimary)
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Text(
                text = "Dashboard Admin",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.tertiary
            )

            Spacer(modifier = Modifier.height(32.dp))

            AdminMenuItem(
                title = "📦 Kelola Produk",
                description = "Tambah, edit, hapus produk",
                onClick = {
                    context.startActivity(Intent(context, ProductManagementActivity::class.java))
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            AdminMenuItem(
                title = "📋 Kelola Pesanan",
                description = "Lihat dan verifikasi pesanan dari user",
                onClick = {
                    context.startActivity(Intent(context, OrderManagementActivity::class.java))
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            AdminMenuItem(
                title = "👥 Kelola Pengguna",
                description = "Kelola data pelanggan",
                onClick = {
                    context.startActivity(Intent(context, UserManagementActivity::class.java))
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            AdminMenuItem(
                title = "👤 Kelola Profil",
                description = "Ubah nama dan password Anda",
                onClick = { context.startActivity(Intent(context, ProfileActivity::class.java)) }
            )

            Spacer(modifier = Modifier.height(32.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Statistik", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.tertiary)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("• Total Produk: $totalProducts", style = MaterialTheme.typography.bodyLarge)
                    Text("• Pesanan Baru: $newOrders", style = MaterialTheme.typography.bodyLarge)
                    Text("• Total Pelanggan: $totalCustomers", style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
    }
}

@Composable
fun AdminMenuItem(title: String, description: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text(description, style = MaterialTheme.typography.bodyMedium)
        }
    }
}
