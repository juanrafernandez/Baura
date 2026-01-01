package com.jrlabs.baura.data.remote

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.jrlabs.baura.data.local.CacheManager
import com.jrlabs.baura.data.model.Question
import com.jrlabs.baura.utils.AppLogger
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * QuestionService - Manages test questions
 * Equivalent to QuestionsService.swift
 *
 * Uses collection "questions" with category filter:
 * - category_profile: Personal profile questions
 * - category_gift: Gift search questions
 * - evaluation: Tried perfume evaluation questions
 */
@Singleton
class QuestionService @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val cacheManager: CacheManager
) {
    companion object {
        const val COLLECTION_QUESTIONS = "questions"
        const val CATEGORY_PROFILE = "category_profile"
        const val CATEGORY_GIFT = "category_gift"
        const val CATEGORY_EVALUATION = "evaluation"
    }

    /**
     * Get questions for personal profile test
     */
    suspend fun getProfileQuestions(): List<Question> {
        return getQuestionsByCategory(CATEGORY_PROFILE)
    }

    /**
     * Get questions for gift search
     */
    suspend fun getGiftQuestions(): List<Question> {
        return getQuestionsByCategory(CATEGORY_GIFT)
    }

    /**
     * Get questions by category
     */
    suspend fun getQuestionsByCategory(category: String): List<Question> {
        val cacheKey = "${CacheManager.KEY_QUESTIONS}_$category"
        val cached = cacheManager.load<List<Question>>(cacheKey)
        if (cached != null) {
            AppLogger.info("QuestionService", "Loaded ${cached.size} questions from cache ($category)")
            return cached
        }

        return try {
            AppLogger.info("QuestionService", "Fetching questions from Firestore, category=$category")

            val snapshot = firestore.collection(COLLECTION_QUESTIONS)
                .whereEqualTo("category", category)
                .get()
                .await()

            AppLogger.info("QuestionService", "Firestore returned ${snapshot.documents.size} documents")

            val questions = snapshot.documents.mapNotNull { doc ->
                try {
                    doc.toObject(Question::class.java)?.copy(id = doc.id)
                } catch (e: Exception) {
                    AppLogger.warning("QuestionService", "Failed to parse question ${doc.id}: ${e.message}")
                    null
                }
            }.sortedBy { it.order }

            AppLogger.info("QuestionService", "Parsed ${questions.size} questions")

            if (questions.isNotEmpty()) {
                cacheManager.save(questions, cacheKey)
            }

            questions
        } catch (e: Exception) {
            AppLogger.error("QuestionService", "Failed to get questions", e)
            emptyList()
        }
    }

    /**
     * Legacy method - now uses category-based approach
     */
    suspend fun getQuestions(
        language: String = "es",
        flowType: String? = null
    ): List<Question> {
        // Map flowType to category
        val category = when (flowType) {
            "profile" -> CATEGORY_PROFILE
            "gift" -> CATEGORY_GIFT
            else -> CATEGORY_PROFILE
        }
        return getQuestionsByCategory(category)
    }

    /**
     * Get questions by experience level
     */
    suspend fun getQuestionsByLevel(
        language: String = "es",
        level: String
    ): List<Question> {
        val allQuestions = getQuestions(language)
        return allQuestions.filter { question ->
            question.flow == level || question.flow == null
        }
    }

    /**
     * Get question by ID
     */
    suspend fun getQuestionById(
        questionId: String,
        language: String = "es"
    ): Question? {
        return try {
            val collectionName = "questions_$language"
            val snapshot = firestore.collection(collectionName)
                .document(questionId)
                .get()
                .await()
            snapshot.toObject(Question::class.java)?.copy(id = snapshot.id)
        } catch (e: Exception) {
            AppLogger.error("QuestionService", "Failed to get question: $questionId", e)
            null
        }
    }

    /**
     * Get evaluation questions (category: evaluation)
     * Used for the tried perfume evaluation flow
     * Uses the same collection as other question functions to avoid permission issues
     */
    suspend fun fetchEvaluationQuestions(language: String = "es"): List<Question> {
        val cacheKey = "${CacheManager.KEY_QUESTIONS}_evaluation"
        val cached = cacheManager.load<List<Question>>(cacheKey)
        if (cached != null) {
            AppLogger.Cache.hit(cacheKey)
            return cached
        }

        return try {
            // Use same collection as other question functions to avoid permission issues
            val snapshot = firestore.collection(COLLECTION_QUESTIONS)
                .whereEqualTo("category", CATEGORY_EVALUATION)
                .get()
                .await()

            val questions = snapshot.documents.mapNotNull { doc ->
                doc.toObject(Question::class.java)?.copy(id = doc.id)
            }.sortedBy { it.order } // Sort in memory instead of using orderBy

            AppLogger.info("QuestionService", "Loaded ${questions.size} evaluation questions")

            // Cache the results
            if (questions.isNotEmpty()) {
                cacheManager.save(questions, cacheKey)
                AppLogger.Cache.save(cacheKey)
            }

            questions
        } catch (e: Exception) {
            AppLogger.error("QuestionService", "Failed to get evaluation questions", e)
            emptyList()
        }
    }

    /**
     * Clear questions cache
     */
    suspend fun clearCache() {
        cacheManager.clearCacheByPattern(CacheManager.KEY_QUESTIONS)
    }
}
