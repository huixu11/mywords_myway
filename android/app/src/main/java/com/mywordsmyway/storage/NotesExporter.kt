package com.mywordsmyway.storage

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import com.mywordsmyway.data.local.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.time.ZoneId
import java.time.format.DateTimeFormatter

data class TextExportFile(
    val uri: Uri,
    val text: String,
)

class NotesExporter(
    private val context: Context,
    private val database: AppDatabase,
) {
    private val dateFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy").withZone(ZoneId.systemDefault())

    suspend fun writeNotesOnlyExport(): TextExportFile = withContext(Dispatchers.IO) {
        val notes = database.conversationDao().getAllNotes()
        val linksByConversation = database.nounDao()
            .getExportLinks()
            .groupBy { it.conversationId }
            .mapValues { (_, rows) -> rows.map { it.noun }.distinct().sorted() }
        val exportText = buildString {
            appendLine("My Words, My Way")
            appendLine("Notes-only export")
            appendLine()
            appendLine("Includes: written notes, dates, linked confirmed nouns")
            appendLine("Does not include: audio files or note images")
            appendLine()
            notes.forEach { note ->
                appendLine(dateFormatter.format(note.createdAt))
                if (note.title.isNotBlank()) {
                    appendLine(note.title)
                }
                appendLine(plainNoteText(note.finalNote))
                val nouns = linksByConversation[note.id].orEmpty()
                if (nouns.isNotEmpty()) {
                    appendLine("Words: ${nouns.joinToString(", ")}")
                }
                appendLine()
            }
        }
        val outputDir = File(context.cacheDir, "exports").apply { mkdirs() }
        val outputFile = File(outputDir, "my_words_my_way_notes.txt")
        outputFile.writeText(exportText)
        TextExportFile(
            uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                outputFile,
            ),
            text = exportText,
        )
    }

    suspend fun writeWordsExport(): TextExportFile = withContext(Dispatchers.IO) {
        val nouns = database.nounDao().getConfirmedNounNames()
        val knots = database.borromeanDao().getKnotsWithWords()
        val exportText = buildString {
            appendLine("My Words, My Way")
            appendLine("Words export")
            appendLine()
            if (nouns.isNotEmpty()) {
                appendLine("Saved words")
                nouns.forEach { noun -> appendLine("- $noun") }
                appendLine()
            }
            appendLine("Borromean knots")
            knots.forEach { knotWithWords ->
                appendLine(knotWithWords.knot.title)
                if (knotWithWords.knot.description.isNotBlank()) {
                    appendLine(knotWithWords.knot.description)
                }
                appendWordsSection("object a fragments", knotWithWords.words.filter { it.registerType == "object_a" })
                appendWordsSection(
                    "Personally important words",
                    knotWithWords.words
                        .filter { it.registerType == "affect" }
                        .sortedWith(compareByDescending { it.importanceWeight }),
                )
                appendWordsSection(
                    "What I truly want",
                    knotWithWords.words
                        .filter { it.registerType == "desire" }
                        .sortedByDescending { it.desireWeight },
                )
                appendLine()
            }
        }
        val outputDir = File(context.cacheDir, "exports").apply { mkdirs() }
        val outputFile = File(outputDir, "my_words_my_way_words.txt")
        outputFile.writeText(exportText)
        TextExportFile(
            uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                outputFile,
            ),
            text = exportText,
        )
    }

    private fun StringBuilder.appendWordsSection(title: String, words: List<com.mywordsmyway.data.local.BorromeanWordEntity>) {
        if (words.isEmpty()) return
        appendLine(title)
        words.forEach { word ->
            val metadata = buildList {
                if (word.objectPartType?.isNotBlank() == true) add("type: ${word.objectPartType}")
                add("importance: ${word.importanceWeight}")
                add("emotional attachment: ${word.emotionalWeight}")
                add("desire: ${word.desireWeight}")
            }.joinToString(", ")
            appendLine("- ${word.text} ($metadata)")
        }
        appendLine()
    }
}
