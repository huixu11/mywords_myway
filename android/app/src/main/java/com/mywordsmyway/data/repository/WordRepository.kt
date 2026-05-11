package com.mywordsmyway.data.repository

import com.mywordsmyway.data.local.NounSuggestionEntity
import com.mywordsmyway.data.local.NounWithLinksEntity
import kotlinx.coroutines.flow.Flow

interface WordRepository {
    fun observeNouns(): Flow<List<NounWithLinksEntity>>
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
}
