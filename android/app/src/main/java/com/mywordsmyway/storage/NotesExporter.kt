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

class NotesExporter(
    private val context: Context,
    private val database: AppDatabase,
) {
    private val dateFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy").withZone(ZoneId.systemDefault())

    suspend fun writeNotesOnlyExport(): Uri = withContext(Dispatchers.IO) {
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
        FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            outputFile,
        )
    }
}
