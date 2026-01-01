package com.jrlabs.baura.data.remote

import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.jrlabs.baura.data.model.TriedPerfume
import com.jrlabs.baura.utils.AppLogger
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * TriedPerfumeService - Manages user's tried perfumes
 * Equivalent to TriedPerfumeService.swift
 */
@Singleton
class TriedPerfumeService @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) {
    private val currentUserId: String?
        get() = auth.currentUser?.uid

    /**
     * Get tried perfumes as Flow
     */
    fun getTriedPerfumesFlow(): Flow<List<TriedPerfume>> = callbackFlow {
        val userId = currentUserId
        if (userId == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val listener = firestore.collection(COLLECTION_USERS)
            .document(userId)
            .collection(COLLECTION_TRIED)
            .orderBy("triedAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    AppLogger.error("TriedPerfumeService", "Error listening to tried perfumes", error)
                    return@addSnapshotListener
                }
                val items = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(TriedPerfume::class.java)?.copy(id = doc.id)
                } ?: emptyList()
                trySend(items)
            }

        awaitClose { listener.remove() }
    }

    /**
     * Get all tried perfumes
     */
    suspend fun getTriedPerfumes(): List<TriedPerfume> {
        val userId = currentUserId ?: return emptyList()

        return try {
            val snapshot = firestore.collection(COLLECTION_USERS)
                .document(userId)
                .collection(COLLECTION_TRIED)
                .orderBy("triedAt", Query.Direction.DESCENDING)
                .get()
                .await()

            snapshot.documents.mapNotNull { doc ->
                doc.toObject(TriedPerfume::class.java)?.copy(id = doc.id)
            }
        } catch (e: Exception) {
            AppLogger.error("TriedPerfumeService", "Failed to get tried perfumes", e)
            emptyList()
        }
    }

    /**
     * Get a specific tried perfume by perfumeId
     */
    suspend fun getTriedPerfume(perfumeId: String): TriedPerfume? {
        val userId = currentUserId ?: return null

        return try {
            val snapshot = firestore.collection(COLLECTION_USERS)
                .document(userId)
                .collection(COLLECTION_TRIED)
                .document(perfumeId) // Using perfumeId as document ID (iOS parity)
                .get()
                .await()

            if (snapshot.exists()) {
                snapshot.toObject(TriedPerfume::class.java)?.copy(id = snapshot.id)
            } else {
                // Fallback: search by perfumeId field
                val querySnapshot = firestore.collection(COLLECTION_USERS)
                    .document(userId)
                    .collection(COLLECTION_TRIED)
                    .whereEqualTo("perfumeId", perfumeId)
                    .limit(1)
                    .get()
                    .await()
                querySnapshot.documents.firstOrNull()?.toObject(TriedPerfume::class.java)?.copy(id = querySnapshot.documents.first().id)
            }
        } catch (e: Exception) {
            AppLogger.error("TriedPerfumeService", "Failed to get tried perfume: $perfumeId", e)
            null
        }
    }

    /**
     * Check if perfume has been tried
     */
    suspend fun hasTried(perfumeId: String): Boolean {
        val userId = currentUserId ?: return false

        return try {
            val snapshot = firestore.collection(COLLECTION_USERS)
                .document(userId)
                .collection(COLLECTION_TRIED)
                .whereEqualTo("perfumeId", perfumeId)
                .limit(1)
                .get()
                .await()
            !snapshot.isEmpty
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Add tried perfume (iOS parity - uses perfumeId as document ID)
     */
    suspend fun addTriedPerfume(triedPerfume: TriedPerfume): Result<Unit> {
        val userId = currentUserId ?: return Result.failure(Exception("No user logged in"))

        return try {
            val itemWithTimestamps = triedPerfume.copy(
                triedAt = triedPerfume.triedAt ?: Timestamp.now(),
                updatedAt = Timestamp.now()
            )

            // Use perfumeId as document ID (iOS parity)
            firestore.collection(COLLECTION_USERS)
                .document(userId)
                .collection(COLLECTION_TRIED)
                .document(triedPerfume.perfumeId)
                .set(itemWithTimestamps)
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            AppLogger.error("TriedPerfumeService", "Failed to add tried perfume", e)
            Result.failure(e)
        }
    }

    /**
     * Add tried perfume (legacy - for backwards compatibility)
     */
    @Deprecated("Use addTriedPerfume(TriedPerfume) instead")
    suspend fun addTriedPerfumeLegacy(
        perfumeId: String,
        rating: Double? = null,
        impressions: String? = null,
        isOwned: Boolean = false,
        perfumeName: String? = null,
        perfumeBrand: String? = null,
        perfumeImageUrl: String? = null
    ): Result<String> {
        val userId = currentUserId ?: return Result.failure(Exception("No user logged in"))

        return try {
            @Suppress("DEPRECATION")
            val item = TriedPerfume(
                perfumeId = perfumeId,
                rating = rating ?: 0.0,
                notes = impressions ?: "",
                isOwned = isOwned,
                perfumeName = perfumeName,
                perfumeBrand = perfumeBrand,
                perfumeImageURL = perfumeImageUrl,
                triedAt = Timestamp.now(),
                updatedAt = Timestamp.now()
            )

            val docRef = firestore.collection(COLLECTION_USERS)
                .document(userId)
                .collection(COLLECTION_TRIED)
                .add(item)
                .await()

            Result.success(docRef.id)
        } catch (e: Exception) {
            AppLogger.error("TriedPerfumeService", "Failed to add tried perfume", e)
            Result.failure(e)
        }
    }

    /**
     * Update tried perfume (iOS parity - uses perfumeId as document ID)
     */
    suspend fun updateTriedPerfume(triedPerfume: TriedPerfume): Result<Unit> {
        val userId = currentUserId ?: return Result.failure(Exception("No user logged in"))

        if (triedPerfume.perfumeId.isBlank()) {
            return Result.failure(Exception("Perfume ID is required"))
        }

        return try {
            val updatedItem = triedPerfume.copy(updatedAt = Timestamp.now())

            // Use perfumeId as document ID (iOS parity)
            firestore.collection(COLLECTION_USERS)
                .document(userId)
                .collection(COLLECTION_TRIED)
                .document(triedPerfume.perfumeId)
                .set(updatedItem)
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            AppLogger.error("TriedPerfumeService", "Failed to update tried perfume", e)
            Result.failure(e)
        }
    }

    /**
     * Remove tried perfume
     */
    suspend fun removeTriedPerfume(perfumeId: String): Result<Unit> {
        val userId = currentUserId ?: return Result.failure(Exception("No user logged in"))

        return try {
            val snapshot = firestore.collection(COLLECTION_USERS)
                .document(userId)
                .collection(COLLECTION_TRIED)
                .whereEqualTo("perfumeId", perfumeId)
                .get()
                .await()

            snapshot.documents.forEach { doc ->
                doc.reference.delete().await()
            }

            Result.success(Unit)
        } catch (e: Exception) {
            AppLogger.error("TriedPerfumeService", "Failed to remove tried perfume", e)
            Result.failure(e)
        }
    }

    /**
     * Get owned perfumes only
     */
    suspend fun getOwnedPerfumes(): List<TriedPerfume> {
        return getTriedPerfumes().filter { it.isOwned }
    }

    companion object {
        private const val COLLECTION_USERS = "users"
        private const val COLLECTION_TRIED = "tried_perfumes"
    }
}
