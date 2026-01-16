package com.ampliguitar.customer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ampliguitar.data.CartRepository
import com.ampliguitar.data.OrderRepository
import com.ampliguitar.model.Cart
import com.ampliguitar.model.PaymentMethod
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CheckoutViewModel : ViewModel() {

    private val cartRepository = CartRepository()
    private val orderRepository = OrderRepository()

    val cart: StateFlow<Cart> = cartRepository.cart
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = Cart()
        )

    private val _checkoutState = MutableStateFlow<CheckoutState>(CheckoutState.Idle)
    val checkoutState = _checkoutState.asStateFlow()

    private val _paymentMethod = MutableStateFlow(PaymentMethod.TRANSFER)
    val paymentMethod = _paymentMethod.asStateFlow()

    fun setPaymentMethod(method: PaymentMethod) {
        _paymentMethod.value = method
    }

    fun placeOrder(shippingAddress: String, paymentProofBase64: String?) {
        viewModelScope.launch {
            _checkoutState.value = CheckoutState.Loading
            val result = orderRepository.placeOrder(
                cart.value,
                shippingAddress,
                _paymentMethod.value,
                paymentProofBase64
            )
            result.onSuccess {
                _checkoutState.value = CheckoutState.Success(it)
                cartRepository.clearCart()
            }.onFailure {
                _checkoutState.value = CheckoutState.Error(it.message ?: "Gagal membuat pesanan.")
            }
        }
    }
}