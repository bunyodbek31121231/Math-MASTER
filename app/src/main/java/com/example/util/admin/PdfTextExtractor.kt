package com.example.util.admin

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.nio.charset.Charset
import java.util.zip.InflaterInputStream

object PdfTextExtractor {

    suspend fun extractText(context: Context, uri: Uri): Result<String> = withContext(Dispatchers.IO) {
        try {
            val inputStream = context.contentResolver.openInputStream(uri)
                ?: return@withContext Result.failure(IllegalArgumentException("Fayl ochib bo'lmadi"))
            
            val bytes = inputStream.use { it.readBytes() }
            extractTextFromBytes(bytes)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun extractTextFromBytes(bytes: ByteArray): Result<String> {
        if (bytes.isEmpty()) {
            return Result.failure(IllegalArgumentException("PDF fayli bo'sh"))
        }

        val headerString = String(bytes.take(20).toByteArray(), Charsets.US_ASCII)
        if (!headerString.contains("%PDF-")) {
            return Result.failure(IllegalArgumentException("Fayl PDF formatida emas yoki buzilgan"))
        }

        // Try extracting using PDFBox if available in classpath
        try {
            val pdfBoxResult = extractWithPdfBoxReflection(bytes)
            if (pdfBoxResult != null && pdfBoxResult.isNotBlank()) {
                return Result.success(pdfBoxResult.trim())
            }
        } catch (_: Throwable) {
            // Fallback to built-in stream parser
        }

        // Built-in Pure Kotlin PDF Content Stream Extractor
        return extractWithBuiltInParser(bytes)
    }

    private fun extractWithPdfBoxReflection(bytes: ByteArray): String? {
        return try {
            val pdDocumentClass = Class.forName("com.tomroush.pdfbox.pdmodel.PDDocument")
            val stripperClass = Class.forName("com.tomroush.pdfbox.text.PDFTextStripper")
            val loadMethod = pdDocumentClass.getMethod("load", InputStream::class.java)
            
            val inputStream = ByteArrayInputStream(bytes)
            val document = loadMethod.invoke(null, inputStream)
            val stripper = stripperClass.getDeclaredConstructor().newInstance()
            val getTextMethod = stripperClass.getMethod("getText", pdDocumentClass)
            
            val text = getTextMethod.invoke(stripper, document) as? String
            
            val closeMethod = pdDocumentClass.getMethod("close")
            closeMethod.invoke(document)
            text
        } catch (e: Throwable) {
            null
        }
    }

    private fun extractWithBuiltInParser(bytes: ByteArray): Result<String> {
        val pdfText = String(bytes, Charsets.ISO_8859_1)
        val extractedBuilder = StringBuilder()
        var hasImageObjects = false

        // Check if PDF contains image XObjects / Streams
        if (pdfText.contains("/Subtype /Image") || pdfText.contains("/Subtype/Image") ||
            pdfText.contains("/DCTDecode") || pdfText.contains("/JPXDecode") ||
            pdfText.contains("/CCITTFaxDecode")) {
            hasImageObjects = true
        }

        // Locate stream blocks in PDF
        val streamRegex = Regex("(?s)stream\r?\n(.*?)endstream")
        val streamMatches = streamRegex.findAll(pdfText)

        for (match in streamMatches) {
            val streamStartIndex = match.groups[1]?.range?.first ?: continue
            val streamEndIndex = match.groups[1]?.range?.last ?: continue
            val rawStreamBytes = bytes.copyOfRange(streamStartIndex, streamEndIndex + 1)

            // Look back up to 300 bytes before 'stream' to inspect dictionary filters
            val headerLookbackStart = maxOf(0, match.range.first - 300)
            val dictHeader = String(bytes.copyOfRange(headerLookbackStart, match.range.first), Charsets.US_ASCII)

            var decompressedBytes: ByteArray? = null

            if (dictHeader.contains("/FlateDecode") || dictHeader.contains("/Fl")) {
                try {
                    decompressedBytes = decompressFlate(rawStreamBytes)
                } catch (_: Exception) {
                    // Ignored if stream is not a valid zlib stream
                }
            } else if (!dictHeader.contains("/Filter")) {
                decompressedBytes = rawStreamBytes
            }

            if (decompressedBytes != null && decompressedBytes.isNotEmpty()) {
                val streamContent = String(decompressedBytes, Charsets.UTF_8)
                val textFromStream = parsePdfContentOperators(streamContent)
                if (textFromStream.isNotBlank()) {
                    extractedBuilder.append(textFromStream).append("\n")
                }
            }
        }

        val resultText = extractedBuilder.toString().trim()

        if (resultText.isNotBlank()) {
            return Result.success(resultText)
        }

        if (hasImageObjects) {
            return Result.failure(Exception("Bu PDF skanerlangan rasm ko'rinishida. OCR kerak."))
        }

        return Result.failure(Exception("PDF fayli bo'sh yoki matn o'qib bo'lmadi"))
    }

    private fun decompressFlate(compressedBytes: ByteArray): ByteArray {
        val inputStream = InflaterInputStream(ByteArrayInputStream(compressedBytes))
        val outputStream = ByteArrayOutputStream()
        val buffer = ByteArray(2048)
        var length: Int
        while (inputStream.read(buffer).also { length = it } != -1) {
            outputStream.write(buffer, 0, length)
        }
        return outputStream.toByteArray()
    }

    private fun parsePdfContentOperators(content: String): String {
        val builder = StringBuilder()
        val btEtRegex = Regex("(?s)BT(.*?)ET")
        val btEtMatches = btEtRegex.findAll(content)

        for (btEt in btEtMatches) {
            val btBlock = btEt.groupValues[1]
            val lines = btBlock.split("\n", "\r")

            for (line in lines) {
                val trimmedLine = line.trim()

                // Check string Tj operator: (Hello World) Tj
                val tjRegex = Regex("\\((.*?)\\)\\s*Tj")
                tjRegex.findAll(trimmedLine).forEach { match ->
                    val rawStr = match.groupValues[1]
                    builder.append(unescapePdfString(rawStr))
                }

                // Check TJ array operator: [(Hello ) -10 (World)] TJ
                val arrayTjRegex = Regex("\\[(.*?)\\]\\s*TJ")
                arrayTjRegex.findAll(trimmedLine).forEach { match ->
                    val arrayContent = match.groupValues[1]
                    val stringInArrayRegex = Regex("\\((.*?)\\)")
                    stringInArrayRegex.findAll(arrayContent).forEach { strMatch ->
                        builder.append(unescapePdfString(strMatch.groupValues[1]))
                    }
                }

                // Check hex strings <48656c6c6f> Tj
                val hexTjRegex = Regex("<([0-9A-Fa-f]+)>\\s*Tj")
                hexTjRegex.findAll(trimmedLine).forEach { match ->
                    val hex = match.groupValues[1]
                    builder.append(hexToString(hex))
                }

                // Line breaks in BT block
                if (trimmedLine.endsWith("T*") || trimmedLine.endsWith("'") || trimmedLine.contains("Td") || trimmedLine.contains("TD")) {
                    builder.append("\n")
                }
            }
            builder.append("\n")
        }

        return builder.toString()
            .replace(Regex("\n{3,}"), "\n\n")
            .trim()
    }

    private fun unescapePdfString(input: String): String {
        val result = StringBuilder()
        var i = 0
        while (i < input.length) {
            val c = input[i]
            if (c == '\\' && i + 1 < input.length) {
                val next = input[i + 1]
                when (next) {
                    'n' -> { result.append('\n'); i += 2 }
                    'r' -> { result.append('\r'); i += 2 }
                    't' -> { result.append('\t'); i += 2 }
                    'b' -> { result.append('\b'); i += 2 }
                    'f' -> { result.append('\u000C'); i += 2 }
                    '(' -> { result.append('('); i += 2 }
                    ')' -> { result.append(')'); i += 2 }
                    '\\' -> { result.append('\\'); i += 2 }
                    else -> {
                        // Handle octal escape e.g. \101 -> 'A'
                        if (next.isDigit()) {
                            var octal = ""
                            var j = i + 1
                            while (j < input.length && j < i + 4 && input[j].isDigit()) {
                                octal += input[j]
                                j++
                            }
                            try {
                                val code = octal.toInt(8)
                                result.append(code.toChar())
                                i = j
                            } catch (_: Exception) {
                                result.append(next)
                                i += 2
                            }
                        } else {
                            result.append(next)
                            i += 2
                        }
                    }
                }
            } else {
                result.append(c)
                i++
            }
        }
        return result.toString()
    }

    private fun hexToString(hex: String): String {
        return try {
            val bytes = ByteArray(hex.length / 2)
            for (i in bytes.indices) {
                val index = i * 2
                bytes[i] = hex.substring(index, index + 2).toInt(16).toByte()
            }
            String(bytes, Charsets.UTF_8)
        } catch (_: Exception) {
            hex
        }
    }
}
