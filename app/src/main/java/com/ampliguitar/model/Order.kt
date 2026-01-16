package com.ampliguitar.model

import com.google.firebase.firestore.DocumentId

enum class PaymentMethod {
    TRANSFER,
    COD
}

data class Order(
    @DocumentId val id: String = "",
    val userId: String = "",
    val userName: String = "", // Ditambahkan
    val items: List<CartItem> = emptyList(),
    val totalPrice: Double = 0.0,
    val shippingAddress: String = "",
    val paymentMethod: PaymentMethod = PaymentMethod.TRANSFER, // Ditambahkan
    val paymentProofBase64: String? = null,
    val status: OrderStatus = OrderStatus.PENDING, // Diubah tipenya
    val createdAt: Long = System.currentTimeMillis(),
    val shippingReceipt: String = "" // Ditambahkan
)
