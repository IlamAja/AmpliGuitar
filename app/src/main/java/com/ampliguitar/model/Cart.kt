package com.ampliguitar.model

import com.google.firebase.firestore.Exclude

data class Cart(
    val items: List<CartItem> = emptyList(),
) {
    @get:Exclude
    val totalPrice: Double
        get() = items.sumOf { it.price * it.quantity }

    @get:Exclude
    val itemCount: Int
        get() = items.sumOf { it.quantity }
}