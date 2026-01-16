package com.ampliguitar.customer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ampliguitar.data.CartRepository
import com.ampliguitar.data.ProductRepository
import com.ampliguitar.model.Product
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainCustomerViewModel : ViewModel() {
    private val productRepository = ProductRepository()
    private val cartRepository = CartRepository()

    private val _searchText = MutableStateFlow("")
    val searchText = _searchText.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching = _isSearching.asStateFlow()

    private val _products = productRepository.products
    val products: StateFlow<List<Product>> = searchText
        .combine(_products) { text, products ->
            if (text.isBlank()) {
                products
            } else {
                products.filter {
                    it.name.contains(text, ignoreCase = true)
                }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val cartItemCount: StateFlow<Int> = cartRepository.cart
        .map { it.items.sumOf { item -> item.quantity } }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

    fun onSearchTextChange(text: String) {
        _searchText.value = text
    }

    fun addToCart(product: Product) {
        viewModelScope.launch {
            cartRepository.addToCart(product, 1)
        }
    }
}
