package com.ampliguitar.model

data class CartItem(
    val productId: String = "",
    val productName: String = "",
    var quantity: Int = 0,
    val price: Double = 0.0,
    val imageBase64: String? = null
)
