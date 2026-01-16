package com.ampliguitar.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ampliguitar.data.ProductRepository
import com.ampliguitar.model.Product
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ProductManagementViewModel : ViewModel() {

    private val productRepository = ProductRepository()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    val products: StateFlow<List<Product>> = combine(
        productRepository.products,
        searchQuery
    ) { products, query ->
        if (query.isBlank()) {
            products
        } else {
            products.filter { it.name.contains(query, ignoreCase = true) }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun addProduct(product: Product, imageBase64: String?) {
        viewModelScope.launch {
            productRepository.addProduct(product.copy(imageBase64 = imageBase64))
        }
    }

    fun updateProduct(product: Product, imageBase64: String?) {
        viewModelScope.launch {
            productRepository.updateProduct(product.copy(imageBase64 = imageBase64))
        }
    }

    fun deleteProduct(productId: String) {
        viewModelScope.launch {
            productRepository.deleteProduct(productId)
        }
    }
}
