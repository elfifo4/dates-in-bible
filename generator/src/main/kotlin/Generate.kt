import java.io.File

/**
 * Reads data/dates.csv and regenerates everything under minified/.
 *
 * Run from the repository root: ./gradlew :generator:run
 *
 * Validation (the build fails on any error):
 * - the <b>…</b> tags in `text` must wrap whole words, and the tagged words must be exactly
 *   the words listed in `words`;
 * - month/day must be in range and month_name must match month;
 * - the same (verse, month, day) must not appear twice.
 *
 * A "word" is a run of characters that are neither whitespace nor a maqaf — the same split
 * the app uses (FindWordRangesInVerseUseCase), so `w` indexes can be resolved against the
 * app's own verse text.
 */

private val MONTH_NAMES = listOf("ניסן", "אייר", "סיון", "תמוז", "אב", "אלול", "תשרי", "חשוון", "כסלו", "טבת", "שבט", "אדר")
private val WORD = Regex("[^\\s־]+")
private const val OPEN = "<b>"
private const val CLOSE = "</b>"

data class Entry(
    val id: Int,
    val b: Int,
    val c: Int,
    val v: Int,
    val month: Int,
    val day: Int,
    val words: List<Int>,
)

fun main() {
    val csv = File("data/dates.csv")
    val rows = parseCsv(csv.readText().removePrefix("﻿"))
    val header = rows.first()
    val errors = mutableListOf<String>()

    val entries = rows.drop(1).filter { row -> row.any { it.isNotBlank() } }.mapNotNull { row ->
        val r = header.zip(row).toMap()
        val id = r.getValue("id")
        fun err(msg: String): Entry? {
            errors += "id $id (${r["book"]} ${r["chapter"]},${r["verse"]}): $msg"
            return null
        }

        val month = r.getValue("month").toInt()
        val day = r.getValue("day").toInt()
        if (month !in 1..12) return@mapNotNull err("month $month out of range")
        if (day !in 1..30) return@mapNotNull err("day $day out of range")
        if (MONTH_NAMES[month - 1] != r["month_name"]) return@mapNotNull err("month_name '${r["month_name"]}' != month $month")

        val words = parseWords(r.getValue("words"))
        val tagged = taggedWords(r.getValue("text")) ?: return@mapNotNull err("<b> tags must wrap whole words")
        if (tagged != words) return@mapNotNull err("tagged words $tagged != words column $words")

        Entry(id.toInt(), r.getValue("b").toInt(), r.getValue("c").toInt(), r.getValue("v").toInt(), month, day, words)
    }

    entries.groupBy { listOf(it.b, it.c, it.v, it.month, it.day) }.filterValues { it.size > 1 }.forEach { (key, dup) ->
        errors += "duplicate date $key in ids ${dup.map { it.id }}"
    }

    if (errors.isNotEmpty()) {
        errors.forEach(System.err::println)
        error("${errors.size} error(s) in ${csv.path}")
    }

    val sorted = entries.sortedWith(compareBy({ it.b }, { it.c }, { it.v }, { it.words.first() }))
    val out = File("minified")
    out.deleteRecursively()

    write(File(out, "all.json"), sorted, prefix = "") { e ->
        "\"m\":${e.month},\"d\":${e.day},"
    }
    sorted.groupBy { it.month }.forEach { (month, inMonth) ->
        write(File(out, "$month/all.json"), inMonth, prefix = "\"month\":$month,") { e -> "\"d\":${e.day}," }
        inMonth.groupBy { it.day }.forEach { (day, inDay) ->
            write(File(out, "$month/$day.json"), inDay, prefix = "\"month\":$month,\"day\":$day,") { "" }
        }
    }

    val files = out.walkTopDown().count { it.isFile }
    println("${sorted.size} dates in ${sorted.distinctBy { Triple(it.b, it.c, it.v) }.size} verses -> $files files in ${out.path}/")
}

private fun write(file: File, list: List<Entry>, prefix: String, extra: (Entry) -> String) {
    val diffVerses = list.distinctBy { Triple(it.b, it.c, it.v) }.size
    val items = list.joinToString(",") { e ->
        "{\"b\":${e.b},\"c\":${e.c},\"v\":${e.v},${extra(e)}\"w\":[${e.words.joinToString(",")}]}"
    }
    file.parentFile.mkdirs()
    file.writeText("{$prefix\"total\":${list.size},\"diff_verses\":$diffVerses,\"list\":[$items]}")
}

/** "2-6,9-14" -> [2,3,4,5,6,9,...,14] */
private fun parseWords(spec: String): List<Int> = spec.split(",").flatMap { part ->
    val bounds = part.trim().split("-").map { it.trim().toInt() }
    (bounds.first()..bounds.last()).toList()
}

/**
 * Returns the 1-based indexes of the words inside <b>…</b>, or null when a tag cuts through
 * a word or the tags are unbalanced.
 */
private fun taggedWords(text: String): List<Int>? {
    val plain = StringBuilder()
    val boldRanges = mutableListOf<IntRange>()
    var i = 0
    var openAt = -1
    while (i < text.length) {
        when {
            text.startsWith(OPEN, i) -> {
                if (openAt >= 0) return null
                openAt = plain.length
                i += OPEN.length
            }
            text.startsWith(CLOSE, i) -> {
                if (openAt < 0) return null
                boldRanges += openAt until plain.length
                openAt = -1
                i += CLOSE.length
            }
            else -> plain.append(text[i++])
        }
    }
    if (openAt >= 0) return null

    val words = WORD.findAll(plain).map { it.range }.toList()
    val result = mutableListOf<Int>()
    for (bold in boldRanges) {
        val inside = words.withIndex().filter { (_, w) -> w.first >= bold.first && w.last <= bold.last }
        if (inside.isEmpty()) return null
        // a tag boundary inside a word would leave part of that word outside the range
        if (words.any { w -> w.first < bold.first && w.last >= bold.first || w.first <= bold.last && w.last > bold.last }) return null
        result += inside.map { it.index + 1 }
    }
    return result
}

/** Minimal RFC 4180 parser: quoted fields, "" escapes, newlines inside quotes. */
private fun parseCsv(text: String): List<List<String>> {
    val rows = mutableListOf<List<String>>()
    var row = mutableListOf<String>()
    val field = StringBuilder()
    var quoted = false
    var i = 0
    while (i < text.length) {
        val ch = text[i]
        if (quoted) {
            if (ch == '"' && text.getOrNull(i + 1) == '"') {
                field.append('"'); i++
            } else if (ch == '"') {
                quoted = false
            } else {
                field.append(ch)
            }
        } else when (ch) {
            '"' -> quoted = true
            ',' -> { row += field.toString(); field.clear() }
            '\r' -> {}
            '\n' -> { row += field.toString(); field.clear(); rows += row; row = mutableListOf() }
            else -> field.append(ch)
        }
        i++
    }
    if (field.isNotEmpty() || row.isNotEmpty()) {
        row += field.toString(); rows += row
    }
    return rows
}
