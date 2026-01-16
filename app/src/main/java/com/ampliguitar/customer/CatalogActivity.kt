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
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ampliguitar.model.Product
import com.ampliguitar.ui.theme.AmpliGuitarTheme

class CatalogActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AmpliGuitarTheme {
                CatalogScreen(onBack = { finish() })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatalogScreen(
    onBack: () -> Unit,
    catalogViewModel: CatalogViewModel = viewModel(),
    cartViewModel: CartViewModel = viewModel()
) {
    val context = LocalContext.current
    val products by catalogViewModel.products.collectAsState()
    val categories by catalogViewModel.categories.collectAsState()
    val searchQuery by catalogViewModel.searchQuery.collectAsState()
    val selectedCategory by catalogViewModel.selectedCategory.collectAsState()
    val cart by cartViewModel.cart.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Katalog Produk") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") } },
                actions = {
                    BadgedBox(badge = { if (cart.itemCount > 0) Badge { Text(cart.itemCount.toString()) } }) {
                        IconButton(onClick = { context.startActivity(Intent(context, CartActivity::class.java)) }) { Icon(Icons.Default.ShoppingCart, "Cart") }
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            TextField(
                value = searchQuery,
                onValueChange = catalogViewModel::onSearchQueryChange,
                label = { Text("Cari produk...") },
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            )
            ScrollableTabRow(selectedTabIndex = categories.indexOf(selectedCategory)) {
                categories.forEach { category ->
                    Tab(selected = category == selectedCategory, onClick = { catalogViewModel.onCategoryChange(category) }) {
                        Text(category, modifier = Modifier.padding(16.dp))
                    }
                }
            }

            if (products.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Tidak ada produk.") }
            } else {
                LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(products, key = { it.id }) { product ->
                        ProductCard(product = product) {
                            cartViewModel.addToCart(product, 1)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProductCard(product: Product, onAddToCart: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(product.name, style = MaterialTheme.typography.titleMedium)
            Text("Kategori: ${product.category}")
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Rp ${product.price}", style = MaterialTheme.typography.bodyLarge)
                Text("Stok: ${product.stock}")
            }
            Button(onClick = onAddToCart, enabled = product.stock > 0, modifier = Modifier.align(Alignment.End)) {
                Text("Tambah ke Keranjang")
            }
        }
    }
}