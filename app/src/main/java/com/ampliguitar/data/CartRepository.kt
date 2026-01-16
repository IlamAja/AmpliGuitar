package com.ampliguitar.data

import com.ampliguitar.model.Cart
import com.ampliguitar.model.CartItem
import com.ampliguitar.model.Product
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.firestore.ktx.toObjects
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class CartRepository {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val productsCollection = db.collection("products")

    private fun getCartDocRef() = auth.currentUser?.uid?.let { db.collection("carts").document(it) }

    val cart: Flow<Cart> = callbackFlow {
        val docRef = getCartDocRef()
        if (docRef == null) {
            trySend(Cart()).isSuccess
            close()
            return@callbackFlow
        }

        val listener = docRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }

            val cart = snapshot?.toObject<Cart>() ?: Cart()
            if (cart.items.isEmpty()) {
                trySend(cart).isSuccess
                return@addSnapshotListener
            }

            launch { 
                try {
                    val productIds = cart.items.map { it.productId }
                    val productSnapshots = productsCollection.whereIn(FieldPath.documentId(), productIds).get().await()
                    val existingProducts = productSnapshots.toObjects<Product>().associateBy { it.id }

                    val validatedItems = mutableListOf<CartItem>()
                    var isCartOutdated = false

                    for (item in cart.items) {
                        val product = existingProducts[item.productId]
                        if (product != null) {
                            if (item.productName != product.name || item.price != product.price || item.imageBase64 != product.imageBase64) {
                                validatedItems.add(
                                    item.copy(
                                        productName = product.name,
                                        price = product.price,
                                        imageBase64 = product.imageBase64
                                    )
                                )
                                isCartOutdated = true
                            } else {
                                validatedItems.add(item)
                            }
                        } else {
                            isCartOutdated = true
                        }
                    }

                    val validatedCart = cart.copy(items = validatedItems)

                    if (isCartOutdated) {
                        docRef.set(validatedCart).await()
                    }

                    trySend(validatedCart).isSuccess

                } catch (e: Exception) {
                    close(e)
                }
            }
        }
        awaitClose { listener.remove() }
    }


    suspend fun addToCart(product: Product, quantity: Int) {
        val docRef = getCartDocRef() ?: return
        val cart = (docRef.get().await().toObject(Cart::class.java) ?: Cart()).let { currentCart ->
            val existingItem = currentCart.items.find { it.productId == product.id }
            val newItems = if (existingItem != null) {
                currentCart.items.map {
                    if (it.productId == product.id) it.copy(quantity = it.quantity + quantity) else it
                }
            } else {
                currentCart.items + CartItem(product.id, product.name, quantity, product.price, product.imageBase64)
            }
            currentCart.copy(items = newItems)
        }
        docRef.set(cart).await()
    }

    suspend fun removeFromCart(productId: String) {
        val docRef = getCartDocRef() ?: return
        val cart = (docRef.get().await().toObject(Cart::class.java) ?: Cart()).let { currentCart ->
            val newItems = currentCart.items.filter { it.productId != productId }
            currentCart.copy(items = newItems)
        }
        docRef.set(cart).await()
    }

    suspend fun updateQuantity(productId: String, newQuantity: Int) {
        if (newQuantity <= 0) {
            removeFromCart(productId)
            return
        }
        val docRef = getCartDocRef() ?: return
        val cart = (docRef.get().await().toObject(Cart::class.java) ?: Cart()).let { currentCart ->
            val newItems = currentCart.items.map {
                if (it.productId == productId) it.copy(quantity = newQuantity) else it
            }
            currentCart.copy(items = newItems)
        }
        docRef.set(cart).await()
    }

    suspend fun clearCart() {
        getCartDocRef()?.set(Cart())?.await()
    }
}