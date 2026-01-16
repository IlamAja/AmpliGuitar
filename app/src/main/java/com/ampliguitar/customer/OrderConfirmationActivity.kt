package com.ampliguitar.customer

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.ampliguitar.ui.theme.AmpliGuitarTheme

class OrderConfirmationActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AmpliGuitarTheme {
                OrderConfirmationScreen(
                    orderId = intent.getStringExtra("order_id") ?: "Unknown",
                    onHomeClick = {
                        // Kembali ke MainCustomerActivity dan clear back stack
                        val intent = Intent(this, MainCustomerActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                        }
                        startActivity(intent)
                        finish()
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderConfirmationScreen(orderId: String, onHomeClick: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Konfirmasi Pesanan") })
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "✅",
                style = MaterialTheme.typography.headlineLarge
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Pesanan Berhasil!",
                style = MaterialTheme.typography.headlineMedium
            )

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text("ID Pesanan:", style = MaterialTheme.typography.titleSmall)
                    Text(orderId, style = MaterialTheme.typography.titleMedium)

                    Spacer(modifier = Modifier.height(16.dp))

                    Text("Status:", style = MaterialTheme.typography.titleSmall)
                    Text("Menunggu Verifikasi", style = MaterialTheme.typography.bodyMedium)

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        "Pesanan Anda akan segera diproses setelah pembayaran diverifikasi oleh admin.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = onHomeClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Filled.Home, contentDescription = "Home")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Kembali ke Beranda")
            }
        }
    }
}