package com.ampliguitar.model

/**
 * Enum class ini diubah untuk menghilangkan custom constructor yang bermasalah dengan Firestore.
 * Properti `displayName` sekarang menggunakan custom getter, yang aman untuk deserialisasi.
 */
enum class OrderStatus {
    PENDING,
    WAITING_CONFIRMATION,
    VERIFIED,
    PROCESSED,
    SHIPPED,
    COMPLETED,
    CANCELLED;

    val displayName: String
        get() = when (this) {
            PENDING -> "Menunggu Pembayaran"
            WAITING_CONFIRMATION -> "Menunggu Konfirmasi"
            VERIFIED -> "Pembayaran Terverifikasi"
            PROCESSED -> "Sedang Diproses"
            SHIPPED -> "Telah Dikirim"
            COMPLETED -> "Selesai"
            CANCELLED -> "Dibatalkan"
        }
}