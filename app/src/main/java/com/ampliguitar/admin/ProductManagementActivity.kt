package com.ampliguitar.admin

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.ampliguitar.model.Product
import com.ampliguitar.ui.theme.AmpliGuitarTheme
import java.io.ByteArrayOutputStream

class ProductManagementActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AmpliGuitarTheme {
                ProductManagementScreen(onBack = { finish() })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductManagementScreen(
    onBack: () -> Unit,
    viewModel: ProductManagementViewModel = viewModel()
) {
    val products by viewModel.products.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    var showProductDialogFor by remember { mutableStateOf<Product?>(null) }
    var isAddingNewProduct by remember { mutableStateOf(false) }
    var productToDelete by remember { mutableStateOf<Product?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Kelola Produk") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") } },
                actions = { IconButton(onClick = { isAddingNewProduct = true }) { Icon(Icons.Default.Add, "Add") } }
            )
        }
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = viewModel::onSearchQueryChange,
                label = { Text("Cari Produk") },
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                trailingIcon = { if (searchQuery.isNotEmpty()) IconButton(onClick = { viewModel.onSearchQueryChange("") }) { Icon(Icons.Default.Close, "Clear") } }
            )
            if (products.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(if (searchQuery.isNotEmpty()) "Produk tidak ditemukan" else "Belum ada produk")
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(products, key = { it.id }) { product ->
                        ProductItem(
                            product = product,
                            onEditClick = { showProductDialogFor = product },
                            onDeleteClick = { productToDelete = product }
                        )
                    }
                }
            }
        }
    }

    if (isAddingNewProduct) {
        ProductFormDialog(
            onDismiss = { isAddingNewProduct = false },
            onConfirm = { product, imageBase64 ->
                viewModel.addProduct(product, imageBase64)
            }
        )
    }

    showProductDialogFor?.let {
        ProductFormDialog(
            product = it,
            onDismiss = { showProductDialogFor = null },
            onConfirm = { product, imageBase64 ->
                viewModel.updateProduct(product, imageBase64)
            }
        )
    }

    productToDelete?.let {
        AlertDialog(
            onDismissRequest = { productToDelete = null },
            title = { Text("Hapus Produk") },
            text = { Text("Yakin ingin menghapus ${it.name}?") },
            confirmButton = {
                TextButton({
                    viewModel.deleteProduct(it.id)
                    productToDelete = null
                }) { Text("Hapus") }
            },
            dismissButton = { TextButton({ productToDelete = null }) { Text("Batal") } }
        )
    }
}

@Composable
fun ProductItem(product: Product, onEditClick: () -> Unit, onDeleteClick: () -> Unit) {
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            val imageModifier = Modifier.size(80.dp)
            if (product.imageBase64 != null) {
                val decodedImage = Base64.decode(product.imageBase64, Base64.DEFAULT)
                val bitmap = android.graphics.BitmapFactory.decodeByteArray(decodedImage, 0, decodedImage.size)
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = product.name,
                    modifier = imageModifier,
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = imageModifier.background(MaterialTheme.colorScheme.secondaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No Img", color = MaterialTheme.colorScheme.onSecondaryContainer)
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(product.name, style = MaterialTheme.typography.titleLarge)
                Text(
                    text = "Rp ${product.price.toBigDecimal().toPlainString()}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Stok: ${product.stock}",
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            Column {
                IconButton(onClick = onEditClick) { Icon(Icons.Default.Edit, "Edit") }
                IconButton(onClick = onDeleteClick) { Icon(Icons.Default.Delete, "Delete") }
            }
        }
    }
}

@Composable
fun ProductFormDialog(
    product: Product? = null,
    onDismiss: () -> Unit,
    onConfirm: (Product, String?) -> Unit
) {
    var name by remember { mutableStateOf(product?.name ?: "") }
    var description by remember { mutableStateOf(product?.description ?: "") }
    var price by remember { mutableStateOf(product?.price?.toBigDecimal()?.toPlainString() ?: "") }
    var stock by remember { mutableStateOf(product?.stock?.toString() ?: "") }
    var category by remember { mutableStateOf(product?.category ?: "") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var imageBase64 by remember(imageUri) { mutableStateOf<String?>(null) }
    val context = LocalContext.current

    LaunchedEffect(imageUri) {
        imageUri?.let {
            imageBase64 = uriToBase64(context, it)
        }
    }

    val imagePicker = rememberLauncherForActivityResult(contract = ActivityResultContracts.PickVisualMedia()) { uri ->
        imageUri = uri
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (product == null) "Tambah Produk" else "Edit Produk") },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                Spacer(modifier = Modifier.height(16.dp))
                // Image Preview
                val imageModifier = Modifier
                    .height(150.dp)
                    .fillMaxWidth()
                    .align(Alignment.CenterHorizontally)

                if (imageUri != null) {
                    Image(painter = rememberAsyncImagePainter(model = imageUri), contentDescription = null, modifier = imageModifier, contentScale = ContentScale.Crop)
                } else if (product?.imageBase64 != null) {
                    val decodedImage = Base64.decode(product.imageBase64, Base64.DEFAULT)
                    val bitmap = android.graphics.BitmapFactory.decodeByteArray(decodedImage, 0, decodedImage.size)
                    Image(bitmap = bitmap.asImageBitmap(), contentDescription = null, modifier = imageModifier, contentScale = ContentScale.Crop)
                }

                Button(onClick = { imagePicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }, modifier = Modifier.align(Alignment.CenterHorizontally)) {
                    Text("Pilih Gambar")
                }

                Spacer(modifier = Modifier.height(16.dp))
                TextField(value = name, onValueChange = { name = it }, label = { Text("Nama") })
                TextField(value = description, onValueChange = { description = it }, label = { Text("Deskripsi") })
                TextField(value = price, onValueChange = { price = it }, label = { Text("Harga") })
                TextField(value = stock, onValueChange = { stock = it }, label = { Text("Stok") })
                TextField(value = category, onValueChange = { category = it }, label = { Text("Kategori") })
            }
        },
        confirmButton = {
            Button(onClick = {
                val updatedProduct = (product ?: Product()).copy(
                    name = name,
                    description = description,
                    price = price.toDoubleOrNull() ?: 0.0,
                    stock = stock.toIntOrNull() ?: 0,
                    category = category
                )
                onConfirm(updatedProduct, imageBase64)
                onDismiss()
            }) { Text(if (product == null) "Tambah" else "Simpan") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Batal") } }
    )
}

fun uriToBase64(context: Context, uri: Uri): String? {
    return try {
        val inputStream = context.contentResolver.openInputStream(uri) ?: return null
        val outputStream = ByteArrayOutputStream()
        val buffer = ByteArray(1024)
        var len: Int
        while (inputStream.read(buffer).also { len = it } != -1) {
            outputStream.write(buffer, 0, len)
        }
        val bytes = outputStream.toByteArray()
        Base64.encodeToString(bytes, Base64.DEFAULT)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}
