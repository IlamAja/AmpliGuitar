package com.ampliguitar.customer

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

class CatalogViewModel : ViewModel() {

    private val productRepository = ProductRepository()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory = _selectedCategory.asStateFlow()

    val products: StateFlow<List<Product>> = combine(
        productRepository.products,
        searchQuery,
        selectedCategory
    ) { products, query, category ->
        products.filter {
            val matchesCategory = category == "All" || it.category == category
            val matchesQuery = query.isBlank() || it.name.contains(query, ignoreCase = true)
            matchesCategory && matchesQuery
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val categories: StateFlow<List<String>> = productRepository.products.combine(MutableStateFlow("All")) { products, allCategory ->
        listOf(allCategory) + products.map { it.category }.distinct()
    }.stateIn(viewModelScope, SharingStarted.Lazily, listOf("All"))

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun onCategoryChange(category: String) {
        _selectedCategory.value = category
    }
}