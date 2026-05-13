package com.mywordsmyway.data.repository

import com.mywordsmyway.data.local.BorromeanKnotWithWords
import com.mywordsmyway.data.local.NounSuggestionEntity
import com.mywordsmyway.data.local.NounWithLinksEntity
import kotlinx.coroutines.flow.Flow

interface WordRepository {
    fun observeNouns(): Flow<List<NounWithLinksEntity>>
    fun observeBorromeanKnots(): Flow<List<BorromeanKnotWithWords>>
    fun observeSuggestions(conversationId: String): Flow<List<NounSuggestionEntity>>
    fun observePendingSuggestions(): Flow<List<NounSuggestionEntity>>
    suspend fun keepSuggestion(suggestionId: String)
    suspend fun renameSuggestion(suggestionId: String, newNoun: String)
    suspend fun rejectSuggestion(suggestionId: String)
    suspend fun renameNoun(nounId: String, newNoun: String)
    suspend fun mergeNoun(sourceNounId: String, targetNounId: String)
    suspend fun deleteNoun(nounId: String)
    suspend fun deleteAllNouns()
    suspend fun deletePendingSuggestions()
    suspend fun createBorromeanKnot(title: String, description: String, personName: String? = null, theoryNote: String = ""): String
    suspend fun renameBorromeanKnot(knotId: String, title: String, description: String, personName: String? = null, theoryNote: String = "")
    suspend fun archiveBorromeanKnot(knotId: String)
    suspend fun addBorromeanWord(
        knotId: String,
        text: String,
        registerType: String,
        objectPartType: String? = null,
        emotionalWeight: Int = 50,
        importanceWeight: Int = 50,
        desireWeight: Int = 50,
    ): String
    suspend fun renameBorromeanWord(wordId: String, text: String)
    suspend fun updateBorromeanWordDetails(
        wordId: String,
        text: String,
        objectPartType: String?,
        emotionalWeight: Int,
        importanceWeight: Int,
        desireWeight: Int,
    )
    suspend fun updateBorromeanWordWeights(wordId: String, emotionalWeight: Int, importanceWeight: Int, desireWeight: Int)
    suspend fun deleteBorromeanWord(wordId: String)
    suspend fun linkBorromeanWordToConversation(wordId: String, conversationId: String)
}
