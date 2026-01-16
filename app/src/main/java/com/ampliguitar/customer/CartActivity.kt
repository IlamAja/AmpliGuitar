package com.ampliguitar.customer

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ampliguitar.model.CartItem
import com.ampliguitar.ui.theme.AmpliGuitarTheme

class CartActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AmpliGuitarTheme {
                CartScreen(
                    onBack = { finish() },
                    onCheckout = { startActivity(Intent(this, CheckoutActivity::class.java)) }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(
    onBack: () -> Unit,
    onCheckout: () -> Unit,
    viewModel: CartViewModel = viewModel()
) {
    val cart by viewModel.cart.collectAsState()

    Scaffold(
        topBar = { TopAppBar(title = { Text("Keranjang Belanja") }, navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") } }) }
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            if (cart.items.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Keranjang belanja kosong.")
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(cart.items, key = { it.productId }) { item ->
                        CartItemCard(
                            item = item,
                            onUpdateQuantity = { newQuantity -> viewModel.updateQuantity(item.productId, newQuantity) },
                            onRemove = { viewModel.removeFromCart(item.productId) }
                        )
                    }
                }
                CartSummary(totalPrice = cart.totalPrice, onCheckout = onCheckout)
            }
        }
    }
}

@Composable
fun CartSummary(totalPrice: Double, onCheckout: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Ringkasan Belanja", style = MaterialTheme.typography.titleMedium)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Subtotal")
                Text("Rp ${"%,.0f".format(totalPrice)}")
            }
            val shippingCost = 50000.0 // Example shipping cost
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Biaya Pengiriman")
                Text("Rp ${"%,.0f".format(shippingCost)}")
            }
            HorizontalDivider()
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Total", style = MaterialTheme.typography.titleMedium)
                Text("Rp ${"%,.0f".format(totalPrice + shippingCost)}", style = MaterialTheme.typography.titleMedium)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = onCheckout, modifier = Modifier.fillMaxWidth()) {
                Text("Lanjut ke Checkout")
            }
        }
    }
}

@Composable
fun CartItemCard(item: CartItem, onUpdateQuantity: (Int) -> Unit, onRemove: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(item.productName, style = MaterialTheme.typography.titleMedium)
                Text("Rp ${"%,.0f".format(item.price)}")
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                IconButton(onClick = { onUpdateQuantity(item.quantity - 1) }, modifier = Modifier.size(24.dp)) { Text("-") }
                Text("${item.quantity}")
                IconButton(onClick = { onUpdateQuantity(item.quantity + 1) }, modifier = Modifier.size(24.dp)) { Text("+") }
                IconButton(onClick = onRemove) { Icon(Icons.Default.Delete, "Remove") }
            }
        }
    }
}
