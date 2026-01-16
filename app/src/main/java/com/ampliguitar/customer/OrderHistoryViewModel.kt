package com.ampliguitar.customer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ampliguitar.data.OrderRepository
import com.ampliguitar.model.OrderStatus
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class OrderHistoryViewModel : ViewModel() {

    private val orderRepository = OrderRepository()

    val uiState: StateFlow<OrderHistoryUiState> = orderRepository.getOrderHistory()
        .map { orders ->
            if (orders.isEmpty()) {
                OrderHistoryUiState.Empty
            } else {
                // Sorting is handled here to prevent database query issues.
                OrderHistoryUiState.Success(orders.sortedByDescending { it.createdAt })
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = OrderHistoryUiState.Loading
        )

    fun confirmOrderReceived(orderId: String) {
        viewModelScope.launch {
            orderRepository.updateOrderStatus(orderId, OrderStatus.COMPLETED)
        }
    }
}