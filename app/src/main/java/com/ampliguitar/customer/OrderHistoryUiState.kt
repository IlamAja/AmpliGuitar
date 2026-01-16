package com.ampliguitar.customer

import com.ampliguitar.model.Order

sealed interface OrderHistoryUiState {
    object Loading : OrderHistoryUiState
    object Empty : OrderHistoryUiState
    data class Success(val orders: List<Order>) : OrderHistoryUiState
}