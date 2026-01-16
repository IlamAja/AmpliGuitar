package com.ampliguitar.data

import com.ampliguitar.model.Cart
import com.ampliguitar.model.Order
import com.ampliguitar.model.OrderStatus
import com.ampliguitar.model.PaymentMethod
import com.ampliguitar.model.Product
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.tasks.await

class OrderRepository {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val authRepository = AuthRepository

    private fun getUserId(): String? = auth.currentUser?.uid

    suspend fun placeOrder(
        cart: Cart,
        shippingAddress: String,
        paymentMethod: PaymentMethod,
        paymentProofBase64: String?
    ): Result<String> {
        val user = authRepository.currentUser.firstOrNull() ?: return Result.failure(Exception("User not logged in"))

        return try {
            val newOrderId = db.runTransaction { transaction ->
                // 1. Create a new order document reference
                val newOrderRef = db.collection("orders").document()

                // 2. For each item in the cart, decrease the product stock
                for (item in cart.items) {
                    val productRef = db.collection("products").document(item.productId)
                    val productSnapshot = transaction.get(productRef)
                    val currentStock = productSnapshot.toObject(Product::class.java)?.stock ?: 0

                    if (currentStock < item.quantity) {
                        throw FirebaseFirestoreException(
                            "Stok untuk ${item.productName} tidak mencukupi. Tersisa: $currentStock",
                            FirebaseFirestoreException.Code.ABORTED
                        )
                    }

                    transaction.update(productRef, "stock", currentStock - item.quantity)
                }

                // 3. If all stock updates are successful, create the order
                val order = Order(
                    id = newOrderRef.id,
                    userId = user.id,
                    userName = user.name,
                    items = cart.items,
                    totalPrice = cart.totalPrice,
                    shippingAddress = shippingAddress,
                    paymentMethod = paymentMethod,
                    paymentProofBase64 = if (paymentMethod == PaymentMethod.COD) null else paymentProofBase64,
                    status = if (paymentMethod == PaymentMethod.COD) OrderStatus.WAITING_CONFIRMATION else OrderStatus.PENDING
                )
                transaction.set(newOrderRef, order)

                // 4. Return the new order ID
                newOrderRef.id
            }.await()

            Result.success(newOrderId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getAllOrders(): Flow<List<Order>> = callbackFlow {
        val listener = db.collection("orders")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val orders = snapshot?.toObjects(Order::class.java) ?: emptyList()
                trySend(orders).isSuccess
            }
        awaitClose { listener.remove() }
    }

    suspend fun updateOrderStatus(orderId: String, newStatus: OrderStatus): Result<Unit> {
        return try {
            db.collection("orders").document(orderId).update("status", newStatus).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun addShippingReceipt(orderId: String, receipt: String): Result<Unit> {
        return try {
            db.collection("orders").document(orderId).update(
                mapOf(
                    "shippingReceipt" to receipt,
                    "status" to OrderStatus.SHIPPED
                )
            ).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getOrderHistory(): Flow<List<Order>> = callbackFlow {
        val userId = getUserId() ?: close(IllegalStateException("User not logged in"))
        val listener = db.collection("orders")
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val orders = snapshot?.toObjects(Order::class.java) ?: emptyList()
                trySend(orders).isSuccess
            }
        awaitClose { listener.remove() }
    }
}