package com.ampliguitar.customer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ampliguitar.data.CartRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class CustomerViewModel : ViewModel() {
    private val cartRepository = CartRepository()

    val cartItemCount: StateFlow<Int> = cartRepository.cart
        .map { it.items.sumOf { item -> item.quantity } }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )
}