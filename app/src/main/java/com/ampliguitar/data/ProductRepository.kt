package com.ampliguitar.data

import com.ampliguitar.model.Product
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class ProductRepository {
    private val db = FirebaseFirestore.getInstance()
    private val productsCollection = db.collection("products")

    val products: Flow<List<Product>> = callbackFlow {
        val listener = productsCollection.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            val productList = snapshot?.toObjects(Product::class.java) ?: emptyList()
            trySend(productList).isSuccess
        }
        awaitClose { listener.remove() }
    }

    fun getProductById(productId: String): Flow<Product?> = callbackFlow {
        val docRef = productsCollection.document(productId)
        val listener = docRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            trySend(snapshot?.toObject(Product::class.java)).isSuccess
        }
        awaitClose { listener.remove() }
    }

    suspend fun addProduct(product: Product) {
        productsCollection.add(product).await()
    }

    suspend fun updateProduct(product: Product) {
        if (product.id.isNotEmpty()) {
            productsCollection.document(product.id).set(product).await()
        }
    }

    suspend fun deleteProduct(productId: String) {
        productsCollection.document(productId).delete().await()
    }
}
