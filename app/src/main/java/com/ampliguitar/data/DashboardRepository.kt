
package com.ampliguitar.data

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

class DashboardRepository {
    private val db: FirebaseFirestore = Firebase.firestore

    suspend fun getTotalProducts(): Int {
        return try {
            val snapshot = db.collection("products").get().await()
            snapshot.size()
        } catch (e: Exception) {
            0
        }
    }

    suspend fun getNewOrders(): Int {
        return try {
            val snapshot = db.collection("orders").whereEqualTo("status", "pending").get().await()
            snapshot.size()
        } catch (e: Exception) {
            0
        }
    }

    suspend fun getTotalCustomers(): Int {
        return try {
            val snapshot = db.collection("users").whereEqualTo("role", "USER").get().await()
            snapshot.size()
        } catch (e: Exception) {
            0
        }
    }
}
