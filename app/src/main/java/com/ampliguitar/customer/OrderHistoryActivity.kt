package com.ampliguitar.customer

import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.ampliguitar.model.Order
import com.ampliguitar.model.OrderStatus
import com.ampliguitar.ui.theme.AmpliGuitarTheme
import java.text.SimpleDateFormat
import java.util.*

class OrderHistoryActivity : ComponentActivity() {
    private val viewModel: OrderHistoryViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AmpliGuitarTheme {
                OrderHistoryScreen(
                    viewModel = viewModel,
                    onBack = { finish() }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderHistoryScreen(viewModel: OrderHistoryViewModel, onBack: () -> Unit) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Riwayat Pesanan") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") } }
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            when (val state = uiState) {
                is OrderHistoryUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is OrderHistoryUiState.Empty -> {
                    Text("Belum ada riwayat pesanan.", modifier = Modifier.align(Alignment.Center))
                }
                is OrderHistoryUiState.Success -> {
                    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        items(state.orders, key = { it.id }) { order ->
                            OrderHistoryCard(order, onConfirm = { viewModel.confirmOrderReceived(order.id) })
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun OrderHistoryCard(order: Order, onConfirm: () -> Unit) {
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()) }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Text("Order #${order.id.take(8).uppercase()}", style = MaterialTheme.typography.titleMedium)
            Text(dateFormat.format(order.createdAt), style = MaterialTheme.typography.bodySmall)
            Spacer(modifier = Modifier.height(8.dp))

            order.items.forEach {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val imageModifier = Modifier.size(64.dp)
                    if (it.imageBase64 != null) {
                        val decodedImage = Base64.decode(it.imageBase64, Base64.DEFAULT)
                        val bitmap = BitmapFactory.decodeByteArray(decodedImage, 0, decodedImage.size)
                        Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = "Gambar Produk",
                            modifier = imageModifier,
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Box(modifier = imageModifier, contentAlignment = Alignment.Center) { Text("No Img") }
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(it.productName, style = MaterialTheme.typography.bodyLarge)
                        Text("Jumlah: ${it.quantity}", style = MaterialTheme.typography.bodyMedium)
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            Text("Total: Rp ${order.totalPrice}", style = MaterialTheme.typography.bodyMedium)
            Text("Status: ${order.status.displayName}", style = MaterialTheme.typography.labelLarge)

            if (order.status == OrderStatus.SHIPPED) {
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onConfirm,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Pesanan Diterima")
                }
            }
        }
    }
}
