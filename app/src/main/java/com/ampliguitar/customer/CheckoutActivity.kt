package com.ampliguitar.customer

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ampliguitar.model.PaymentMethod
import com.ampliguitar.ui.theme.AmpliGuitarTheme
import java.io.ByteArrayOutputStream

class CheckoutActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AmpliGuitarTheme {
                CheckoutScreen(
                    onBack = { finish() },
                    onOrderSuccess = { _ ->
                        // Navigate to a confirmation screen or back to the main activity
                        finish()
                    }
                )
            }
        }
    }
}

// Helper function to convert URI to Base64
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutScreen(
    onBack: () -> Unit,
    onOrderSuccess: (String) -> Unit,
    viewModel: CheckoutViewModel = viewModel()
) {
    val cart by viewModel.cart.collectAsState()
    val checkoutState by viewModel.checkoutState.collectAsState()
    val selectedPaymentMethod by viewModel.paymentMethod.collectAsState()

    var shippingAddress by remember { mutableStateOf("") }
    var paymentProofUri by remember { mutableStateOf<Uri?>(null) }
    var paymentProofBase64 by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current

    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        paymentProofUri = uri
        if (uri != null) {
            paymentProofBase64 = uriToBase64(context, uri)
        }
    }

    LaunchedEffect(checkoutState) {
        (checkoutState as? CheckoutState.Success)?.let {
            onOrderSuccess(it.orderId)
        }
    }

    val isButtonEnabled = checkoutState !is CheckoutState.Loading &&
            shippingAddress.isNotBlank() &&
            (selectedPaymentMethod == PaymentMethod.COD || (selectedPaymentMethod == PaymentMethod.TRANSFER && paymentProofUri != null))

    Scaffold(
        topBar = { TopAppBar(title = { Text("Checkout") }, navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") } }) },
        bottomBar = {
            Column(modifier = Modifier.padding(16.dp)) {
                Button(
                    onClick = { viewModel.placeOrder(shippingAddress, paymentProofBase64) },
                    enabled = isButtonEnabled,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (checkoutState is CheckoutState.Loading) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    } else {
                        Text("Buat Pesanan")
                    }
                }
                (checkoutState as? CheckoutState.Error)?.let {
                    Text(it.message, color = MaterialTheme.colorScheme.error)
                }
            }
        }
    ) { paddingValues ->
        LazyColumn(modifier = Modifier.padding(paddingValues), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            item { Text("Item Pesanan", style = MaterialTheme.typography.titleMedium) }
            items(cart.items) { item ->
                Text("${item.productName} x${item.quantity} - Rp ${(item.price * item.quantity).toBigDecimal().toPlainString()}")
            }
            item { HorizontalDivider() }
            item { Text("Total: Rp ${cart.totalPrice.toBigDecimal().toPlainString()}", style = MaterialTheme.typography.titleLarge) }

            item { Text("Alamat Pengiriman", style = MaterialTheme.typography.titleMedium) }
            item { TextField(shippingAddress, { shippingAddress = it }, label = { Text("Alamat Lengkap") }, modifier = Modifier.fillMaxWidth()) }

            item { Text("Metode Pembayaran", style = MaterialTheme.typography.titleMedium) }
            item {
                Column {
                    PaymentMethod.values().forEach { method ->
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .selectable(selected = (selectedPaymentMethod == method), onClick = { viewModel.setPaymentMethod(method) })
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(selected = (selectedPaymentMethod == method), onClick = { viewModel.setPaymentMethod(method) })
                            Text(
                                text = method.name,
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                    }
                }
            }

            if (selectedPaymentMethod == PaymentMethod.TRANSFER) {
                item { Text("Bukti Pembayaran", style = MaterialTheme.typography.titleMedium) }
                item {
                    Button(onClick = { imagePicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }) {
                        Text(if (paymentProofUri == null) "Pilih Gambar" else "Ganti Gambar")
                    }
                    paymentProofUri?.let { Text("Gambar telah dipilih.") }
                }
            }
        }
    }
}