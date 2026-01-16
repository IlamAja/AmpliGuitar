package com.ampliguitar.customer

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ampliguitar.data.CartRepository
import com.ampliguitar.data.ProductRepository
import com.ampliguitar.model.Product
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ProductDetailViewModel(savedStateHandle: SavedStateHandle) : ViewModel() {

    private val productId: String = savedStateHandle.get<String>("PRODUCT_ID")!!
    private val productRepository = ProductRepository()
    private val cartRepository = CartRepository()

    val product: StateFlow<Product> = productRepository.getProductById(productId)
        .filterNotNull() // Ensure we don't proceed with a null product
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            // A default 'empty' product can be used here if needed, but filterNotNull is better
            // For the initial value, we need a non-null Product. Let's create a temporary one.
            initialValue = Product(id = productId, name = "Loading...")
        )

    fun addToCart(product: Product, onFinished: () -> Unit) {
        viewModelScope.launch {
            cartRepository.addToCart(product, 1)
            onFinished()
        }
    }
}
