package com.ampliguitar.customer

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ampliguitar.auth.LoginActivity
import com.ampliguitar.data.AuthRepository
import com.ampliguitar.model.Product
import com.ampliguitar.profile.ProfileActivity
import com.ampliguitar.ui.theme.AmpliGuitarTheme

class MainCustomerActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AmpliGuitarTheme {
                CustomerDashboard()
            }
        }
    }
}

@Composable
fun ProductCard(
    product: Product,
    onCardClick: () -> Unit,
    onAddToCartClick: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onCardClick),
    ) {
        Column {
            val imageModifier = Modifier
                .height(180.dp)
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
                Box(
                    modifier = imageModifier.background(MaterialTheme.colorScheme.secondaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Error,
                        contentDescription = "No Image",
                        tint = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.size(48.dp)
                    )
                }
            }

            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = product.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2, // Allow 2 lines for product name
                    minLines = 2, // Keep height consistent
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Rp ${product.price.toBigDecimal().toPlainString()}",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                    FilledTonalIconButton(onClick = onAddToCartClick) {
                        Icon(
                            imageVector = Icons.Default.ShoppingCart,
                            contentDescription = "Add to Cart",
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ProductGrid(mainCustomerViewModel: MainCustomerViewModel = viewModel()) {
    val products by mainCustomerViewModel.products.collectAsState()
    val context = LocalContext.current

    if (products.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
            Text("Memuat produk...", modifier = Modifier.padding(top = 60.dp), style = MaterialTheme.typography.bodyLarge)
        }
    } else {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(products) { product ->
                ProductCard(
                    product = product,
                    onCardClick = {
                        val intent = Intent(context, ProductDetailActivity::class.java).apply {
                            putExtra("PRODUCT_ID", product.id)
                        }
                        context.startActivity(intent)
                    },
                    onAddToCartClick = {
                        mainCustomerViewModel.addToCart(product)
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerDashboard(mainCustomerViewModel: MainCustomerViewModel = viewModel()) {
    val context = LocalContext.current
    val authRepository = AuthRepository
    val currentUser by authRepository.currentUser.collectAsState()
    val cartItemCount by mainCustomerViewModel.cartItemCount.collectAsState()
    val searchText by mainCustomerViewModel.searchText.collectAsState()


    LaunchedEffect(currentUser) {
        if (authRepository.currentUser.value == null && currentUser == null) {
            val intent = Intent(context, LoginActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("AmpliGuitar", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                actions = {
                    Text("Hi, ${currentUser?.name ?: "User"}")
                    Spacer(modifier = Modifier.width(8.dp))
                    TextButton(onClick = {
                        authRepository.logout()
                        val intent = Intent(context, LoginActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        }
                        context.startActivity(intent)
                    }) {
                        Text("Logout", color = MaterialTheme.colorScheme.onPrimary)
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar(containerColor = MaterialTheme.colorScheme.primary) {
                val activeColor = MaterialTheme.colorScheme.tertiary
                val inactiveColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
                NavigationBarItem(selected = true, onClick = {}, icon = { Icon(Icons.Default.Home, null, tint = activeColor) }, label = { Text("Beranda", color = activeColor) })
                NavigationBarItem(selected = false, onClick = { context.startActivity(Intent(context, CartActivity::class.java)) }, icon = { BadgedBox(badge = { if (cartItemCount > 0) Badge { Text(cartItemCount.toString()) } }) { Icon(Icons.Default.ShoppingCart, null, tint = inactiveColor) } }, label = { Text("Keranjang", color = inactiveColor) })
                NavigationBarItem(selected = false, onClick = { context.startActivity(Intent(context, OrderHistoryActivity::class.java)) }, icon = { Icon(Icons.AutoMirrored.Filled.List, null, tint = inactiveColor) }, label = { Text("Pesanan", color = inactiveColor) })
                NavigationBarItem(selected = false, onClick = { context.startActivity(Intent(context, ProfileActivity::class.java)) }, icon = { Icon(Icons.Default.Person, null, tint = inactiveColor) }, label = { Text("Profil", color = inactiveColor) })
            }
        }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues).background(MaterialTheme.colorScheme.background)) {
            OutlinedTextField(
                value = searchText,
                onValueChange = mainCustomerViewModel::onSearchTextChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text(text = "Cari produk...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search Icon") },
                trailingIcon = {
                    if (searchText.isNotEmpty()) {
                        IconButton(onClick = { mainCustomerViewModel.onSearchTextChange("") }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear search")
                        }
                    }
                },
                singleLine = true
            )
            ProductGrid(mainCustomerViewModel = mainCustomerViewModel)
        }
    }
}
