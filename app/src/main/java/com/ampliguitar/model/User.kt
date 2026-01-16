package com.ampliguitar.model

/**
 * Firestore memerlukan konstruktor tanpa argumen untuk deserialisasi.
 * Dengan memberikan nilai default ke SEMUA properti, Kotlin secara otomatis
 * akan membuatkan konstruktor tersebut.
 */
data class User(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val password: String = "",
    val role: UserRole = UserRole.USER, // Ditambahkan nilai default
    val createdAt: Long = System.currentTimeMillis()
)