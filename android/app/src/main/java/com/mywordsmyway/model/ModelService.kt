package com.mywordsmyway.model

import com.mywordsmyway.data.model.BorromeanExtractionResult
import com.mywordsmyway.data.model.NounExtractionResult
import com.mywordsmyway.data.model.SafetyResult

interface ModelService {
    suspend fun transcribe(audioPath: String): String
    suspend fun safetyCheck(text: String): SafetyResult
    suspend fun extractNouns(sourceText: String, existingNouns: List<String>): NounExtractionResult
    suspend fun extractBorromeanWords(sourceText: String, existingWords: List<String>): BorromeanExtractionResult
    suspend fun modelStatus(): String = "Mock model"
}
