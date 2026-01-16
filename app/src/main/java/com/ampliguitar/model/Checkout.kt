package com.ampliguitar.model

import java.util.Date

data class CheckoutItem(
    val productId: String,
    val productName: String,
    val price: Double,
    val quantity: Int
)

data class Checkout(
    val id: String = "",
    val userId: String = "",
    val items: List<CheckoutItem> = emptyList(),
    val totalPrice: Double = 0.0,
    val shippingAddress: String = "",
    val status: String = "pending", // pending, verified, processed, shipped, completed, cancelled
    val paymentProof: String = "",
    val shippingReceipt: String = "",
    val createdAt: Date = Date(),
    val updatedAt: Date = Date()
)