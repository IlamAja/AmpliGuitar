package com.ampliguitar.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ampliguitar.data.OrderRepository
import com.ampliguitar.model.Order
import com.ampliguitar.model.OrderStatus
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class OrderManagementViewModel : ViewModel() {

    private val orderRepository = OrderRepository()

    val orders: StateFlow<List<Order>> = orderRepository.getAllOrders()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun updateOrderStatus(orderId: String, newStatus: OrderStatus) {
        viewModelScope.launch {
            orderRepository.updateOrderStatus(orderId, newStatus)
        }
    }

    fun addShippingReceipt(orderId: String, receipt: String) {
        viewModelScope.launch {
            orderRepository.addShippingReceipt(orderId, receipt)
        }
    }
}