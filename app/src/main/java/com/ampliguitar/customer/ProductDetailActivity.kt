package com.ampliguitar.customer

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ampliguitar.model.Product
import com.ampliguitar.ui.theme.AmpliGuitarTheme
import kotlinx.coroutines.launch

class ProductDetailActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AmpliGuitarTheme {
                ProductDetailScreen(onBack = { finish() })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailScreen(onBack: () -> Unit, viewModel: ProductDetailViewModel = viewModel()) {
    val product by viewModel.product.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(product.name, maxLines = 1) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        bottomBar = {
            BottomAppBar(
                modifier = Modifier.padding(16.dp),
                containerColor = MaterialTheme.colorScheme.background
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedButton(onClick = {
                        viewModel.addToCart(product) {
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("${product.name} ditambahkan ke keranjang")
                            }
                        }
                    }, modifier = Modifier.weight(1f)) {
                        Text("Tambah Keranjang")
                    }
                    Button(onClick = {
                        viewModel.addToCart(product) {
                            context.startActivity(Intent(context, CartActivity::class.java))
                        }
                    }, modifier = Modifier.weight(1f)) {
                        Text("Beli Sekarang")
                    }
                }
            }
        }
    ) { paddingValues ->
        if (product.name == "Loading...") {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                val imageModifier = Modifier
                    .height(300.dp)
                    .fillMaxWidth()

                val imageBitmap = remember(product.imageBase64) {
                    try {
                        product.imageBase64?.let {
                            val decodedBytes = Base64.decode(it, Base64.DEFAULT)
                            BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)?.asImageBitmap()
                        }
                    } catch (e: Exception) {
                        null
                    }
                }

                if (imageBitmap != null) {
                    Image(
                        bitmap = imageBitmap,
                        contentDescription = product.name,
                        modifier = imageModifier,
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(modifier = imageModifier.background(MaterialTheme.colorScheme.surfaceVariant))
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = product.name,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Rp ${product.price.toBigDecimal().toPlainString()}",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = product.description,
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(modifier = Modifier.height(80.dp)) // Extra space to avoid overlap with bottom bar
            }
        }
    }
}
