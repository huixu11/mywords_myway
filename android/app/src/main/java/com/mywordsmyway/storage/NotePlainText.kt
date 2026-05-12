package com.mywordsmyway.storage

private val attachmentTokenRegex = Regex("\\[\\[(image|drawing|audio|file):([^\\]]+)]]")
private val tableTokenRegex = Regex("\\[\\[table:([^\\]]+)]]")

fun plainNoteText(source: String): String {
    val output = StringBuilder()
    var lineStart = 0
    while (lineStart < source.length) {
        val lineEnd = source.indexOf('\n', lineStart).let { if (it == -1) source.length else it }
        val line = source.substring(lineStart, lineEnd)
        var index = lineStart + paragraphPrefixLength(line)
        while (index < lineEnd) {
            when {
                attachmentTokenRegex.find(source, index)?.takeIf { it.range.first == index && it.range.last < lineEnd } != null -> {
                    index = requireNotNull(attachmentTokenRegex.find(source, index)).range.last + 1
                }
                tableTokenRegex.find(source, index)?.takeIf { it.range.first == index && it.range.last < lineEnd } != null -> {
                    val match = requireNotNull(tableTokenRegex.find(source, index))
                    output.append(renderTablePayload(match.groupValues[1]))
                    index = match.range.last + 1
                }
                source.startsWith("**", index) -> index += 2
                source.startsWith("~~", index) -> index += 2
                source.startsWith("<u>", index) -> index += 3
                source.startsWith("</u>", index) -> index += 4
                source[index] == '*' -> index += 1
                else -> {
                    output.append(source[index])
                    index++
                }
            }
        }
        if (lineEnd < source.length) {
            output.append('\n')
            lineStart = lineEnd + 1
        } else {
            lineStart = lineEnd
        }
    }
    return output.toString().trim()
}

private fun paragraphPrefixLength(line: String): Int =
    when {
        line.startsWith("### ") -> 4
        line.startsWith("## ") -> 3
        line.startsWith("# ") -> 2
        line.startsWith("> ") -> 2
        line.startsWith("- ") -> 2
        else -> Regex("^\\d+\\.\\s").find(line)?.value?.length ?: 0
    }

private fun renderTablePayload(payload: String): String =
    payload.split(";;").joinToString("\n") { row ->
        row.split("|").joinToString(" | ")
    }
