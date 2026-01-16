
package com.ampliguitar.admin

import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ampliguitar.model.Order
import com.ampliguitar.model.OrderStatus
import com.ampliguitar.ui.theme.AmpliGuitarTheme
import java.text.SimpleDateFormat
import java.util.Locale

class OrderManagementActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AmpliGuitarTheme {
                OrderManagementScreen(
                    onBack = { finish() }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderManagementScreen(
    onBack: () -> Unit,
    viewModel: OrderManagementViewModel = viewModel()
) {
    val orders by viewModel.orders.collectAsState()
    var selectedOrder by remember { mutableStateOf<Order?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Kelola Pesanan") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (orders.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text("Belum ada pesanan.")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(orders, key = { it.id }) {
                    OrderCard(order = it) { selectedOrder = it }
                }
            }
        }
    }

    selectedOrder?.let {
        OrderDetailDialog(
            order = it,
            onDismiss = { selectedOrder = null },
            onUpdateStatus = { newStatus ->
                viewModel.updateOrderStatus(it.id, newStatus)
                selectedOrder = null
            },
            onAddShippingReceipt = { receipt ->
                viewModel.addShippingReceipt(it.id, receipt)
                selectedOrder = null
            }
        )
    }
}

@Composable
fun Base64Image(base64String: String, modifier: Modifier = Modifier) {
    val decodedBytes = try {
        Base64.decode(base64String, Base64.DEFAULT)
    } catch (e: IllegalArgumentException) {
        e.printStackTrace()
        null
    }

    decodedBytes?.let { bytes ->
        val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        bitmap?.let {
            Image(
                bitmap = it.asImageBitmap(),
                contentDescription = "Bukti Pembayaran",
                modifier = modifier.fillMaxWidth(),
                contentScale = ContentScale.Fit
            )
        }
    }
}

@Composable
fun OrderCard(order: Order, onClick: () -> Unit) {
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Order ID: ${order.id}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Pemesan: ${order.userName}",
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Tanggal: ${dateFormat.format(order.createdAt)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Total: Rp${order.totalPrice}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Status: ${order.status.displayName}",
                style = MaterialTheme.typography.bodyMedium,
                color = when (order.status) {
                    OrderStatus.COMPLETED -> Color(0xFF388E3C) // Hijau
                    OrderStatus.CANCELLED -> Color.Red
                    else -> MaterialTheme.colorScheme.primary
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderDetailDialog(
    order: Order,
    onDismiss: () -> Unit,
    onUpdateStatus: (OrderStatus) -> Unit,
    onAddShippingReceipt: (String) -> Unit
) {
    var shippingReceipt by remember(order) { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Detail Order #${order.id.take(8).uppercase()}") },
        text = {
            LazyColumn {
                item {
                    Text("Pemesan: ${order.userName}", fontWeight = FontWeight.Bold)
                    Text("Alamat: ${order.shippingAddress}")
                    Spacer(modifier = Modifier.height(8.dp))
                }

                items(order.items) {
                    Text("${it.productName} x${it.quantity} - Rp ${it.price * it.quantity}")
                }

                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Total: Rp${order.totalPrice}", fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(16.dp))
                }

                item {
                    order.paymentProofBase64?.let { base64Image ->
                        if (base64Image.isNotBlank()) {
                            Text("Bukti Pembayaran:", fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(8.dp))
                            Base64Image(base64String = base64Image)
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }
                }

                item {
                    Text("Status: ${order.status.displayName}")
                    Spacer(modifier = Modifier.height(16.dp))
                    when (order.status) {
                        OrderStatus.PENDING -> Button(onClick = { onUpdateStatus(OrderStatus.VERIFIED) }) { Text("Verifikasi Pembayaran") }
                        OrderStatus.WAITING_CONFIRMATION -> Button(onClick = { onUpdateStatus(OrderStatus.PROCESSED) }) { Text("Konfirmasi Pesanan") }
                        OrderStatus.VERIFIED -> Button(onClick = { onUpdateStatus(OrderStatus.PROCESSED) }) { Text("Proses Pesanan") }
                        OrderStatus.PROCESSED -> {
                            OutlinedTextField(value = shippingReceipt, onValueChange = { shippingReceipt = it }, label = { Text("Nomor Resi") })
                            Button(onClick = { onAddShippingReceipt(shippingReceipt) }, enabled = shippingReceipt.isNotBlank()) { Text("Kirim & Input Resi") }
                        }
                        else -> {}
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Tutup") } }
    )
}
