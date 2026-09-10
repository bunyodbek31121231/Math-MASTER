package com.example.util.admin

object RuleBasedMaterialAnalyzer {

    fun analyzeText(text: String): List<AnalyzedMaterial> {
        if (text.isBlank()) return emptyList()

        val materials = mutableListOf<AnalyzedMaterial>()
        val lines = text.split("\n").map { it.trim() }.filter { it.isNotEmpty() }

        var currentSectionTitle: String? = null
        var currentTopicTitle: String? = null

        var i = 0
        while (i < lines.size) {
            val line = lines[i]

            // 1. SECTION detection
            if (isSectionHeader(line)) {
                currentSectionTitle = line
                materials.add(
                    AnalyzedMaterial(
                        type = "SECTION",
                        title = line,
                        sectionTitle = line,
                        isUncertain = false
                    )
                )
                i++
                continue
            }

            // 2. TOPIC detection
            if (isTopicHeader(line)) {
                currentTopicTitle = line
                materials.add(
                    AnalyzedMaterial(
                        type = "TOPIC",
                        title = line,
                        sectionTitle = currentSectionTitle,
                        topicTitle = line,
                        isUncertain = false
                    )
                )
                i++
                continue
            }

            // 3. TEST (Multiple Choice) detection e.g. 1. Question... A) ... B) ... C) ... D) ...
            if (isTestStart(line)) {
                val testBlock = parseTestBlock(lines, i, currentSectionTitle, currentTopicTitle)
                materials.add(testBlock.material)
                i = testBlock.nextIndex
                continue
            }

            // 4. EXAMPLE detection e.g. 1-misol. or Misol:
            if (isExampleStart(line)) {
                val exampleBlock = parseExampleBlock(lines, i, currentSectionTitle, currentTopicTitle)
                materials.add(exampleBlock.material)
                i = exampleBlock.nextIndex
                continue
            }

            // 5. EXERCISE detection e.g. 1-mashq. or Mashq:
            if (isExerciseStart(line)) {
                val exerciseBlock = parseExerciseBlock(lines, i, currentSectionTitle, currentTopicTitle)
                materials.add(exerciseBlock.material)
                i = exerciseBlock.nextIndex
                continue
            }

            // 6. FORMULA detection e.g. contains math operators/equations or $...$
            if (isFormulaLine(line)) {
                materials.add(
                    AnalyzedMaterial(
                        type = "FORMULA",
                        formula = line,
                        body = line,
                        sectionTitle = currentSectionTitle,
                        topicTitle = currentTopicTitle,
                        isUncertain = false
                    )
                )
                i++
                continue
            }

            // 7. THEORY / Default text paragraph
            val theoryBlock = parseTheoryBlock(lines, i, currentSectionTitle, currentTopicTitle)
            materials.add(theoryBlock.material)
            i = theoryBlock.nextIndex
        }

        return materials
    }

    private fun isSectionHeader(line: String): Boolean {
        val lower = line.lowercase()
        return lower.startsWith("bo'lim") || lower.startsWith("bolim") ||
                lower.matches(Regex("^(i|ii|iii|iv|v|vi|vii|viii|ix|x)\\b.*bo['`’]lim.*", RegexOption.IGNORE_CASE)) ||
                lower.startsWith("chapter") || lower.startsWith("1-bo'lim") || lower.startsWith("2-bo'lim")
    }

    private fun isTopicHeader(line: String): Boolean {
        val lower = line.lowercase()
        return lower.startsWith("mavzu") || lower.startsWith("1-mavzu") || lower.startsWith("2-mavzu") ||
                lower.startsWith("§") || lower.matches(Regex("^\\d+\\.\\d+\\s+.*"))
    }

    private fun isTestStart(line: String): Boolean {
        return line.matches(Regex("^\\d+[\\.\\)]\\s+.*")) && !isExampleStart(line) && !isExerciseStart(line)
    }

    private fun isExampleStart(line: String): Boolean {
        val lower = line.lowercase()
        return lower.startsWith("misol") || lower.matches(Regex("^\\d+-misol.*")) || lower.startsWith("namuna")
    }

    private fun isExerciseStart(line: String): Boolean {
        val lower = line.lowercase()
        return lower.startsWith("mashq") || lower.matches(Regex("^\\d+-mashq.*")) || lower.startsWith("topshiriq")
    }

    private fun isFormulaLine(line: String): Boolean {
        return line.contains("=") && (line.contains("+") || line.contains("-") || line.contains("*") || line.contains("^") || line.contains("/"))
    }

    private class ParsedBlock(val material: AnalyzedMaterial, val nextIndex: Int)

    private fun parseTestBlock(lines: List<String>, startIndex: Int, section: String?, topic: String?): ParsedBlock {
        val questionBuilder = StringBuilder(lines[startIndex])
        var optionA: String? = null
        var optionB: String? = null
        var optionC: String? = null
        var optionD: String? = null
        var correctAnswer: String? = null
        var solution: String? = null
        var isUncertain = false

        var curr = startIndex + 1
        while (curr < lines.size) {
            val line = lines[curr]
            if (isSectionHeader(line) || isTopicHeader(line) || isTestStart(line) || isExampleStart(line) || isExerciseStart(line)) {
                break
            }

            if (line.contains("A)") || line.contains("A.")) optionA = extractOption(line, "A")
            if (line.contains("B)") || line.contains("B.")) optionB = extractOption(line, "B")
            if (line.contains("C)") || line.contains("C.")) optionC = extractOption(line, "C")
            if (line.contains("D)") || line.contains("D.")) optionD = extractOption(line, "D")

            if (line.lowercase().startsWith("javob:") || line.lowercase().startsWith("ans:")) {
                correctAnswer = line.substringAfter(":").trim().take(1).uppercase()
            } else if (!line.contains("A)") && !line.contains("B)") && !line.contains("C)") && !line.contains("D)")) {
                questionBuilder.append(" ").append(line)
            }

            curr++
        }

        if (optionA == null || optionB == null || optionC == null || optionD == null) {
            isUncertain = true
        }

        val questionText = questionBuilder.toString().replace(Regex("^\\d+[\\.\\)]\\s*"), "").trim()

        val material = AnalyzedMaterial(
            type = "TEST",
            question = questionText,
            body = questionText,
            optionA = optionA ?: "A) Variant A",
            optionB = optionB ?: "B) Variant B",
            optionC = optionC ?: "C) Variant C",
            optionD = optionD ?: "D) Variant D",
            correctAnswer = correctAnswer ?: "A",
            solution = solution,
            sectionTitle = section,
            topicTitle = topic,
            isUncertain = isUncertain
        )

        return ParsedBlock(material, curr)
    }

    private fun extractOption(line: String, optLetter: String): String {
        val pattern = Regex("(?i)$optLetter[\\.\\)]\\s*(.*?)(?=[A-D][\\.\\)]|$)")
        val match = pattern.find(line)
        return match?.groupValues?.get(1)?.trim() ?: line.trim()
    }

    private fun parseExampleBlock(lines: List<String>, startIndex: Int, section: String?, topic: String?): ParsedBlock {
        val title = lines[startIndex]
        val bodyBuilder = StringBuilder()
        var curr = startIndex + 1

        while (curr < lines.size) {
            val line = lines[curr]
            if (isSectionHeader(line) || isTopicHeader(line) || isTestStart(line) || isExampleStart(line) || isExerciseStart(line)) {
                break
            }
            bodyBuilder.append(line).append("\n")
            curr++
        }

        val material = AnalyzedMaterial(
            type = "EXAMPLE",
            title = title,
            body = bodyBuilder.toString().trim(),
            sectionTitle = section,
            topicTitle = topic,
            isUncertain = false
        )

        return ParsedBlock(material, curr)
    }

    private fun parseExerciseBlock(lines: List<String>, startIndex: Int, section: String?, topic: String?): ParsedBlock {
        val title = lines[startIndex]
        val bodyBuilder = StringBuilder()
        var curr = startIndex + 1

        while (curr < lines.size) {
            val line = lines[curr]
            if (isSectionHeader(line) || isTopicHeader(line) || isTestStart(line) || isExampleStart(line) || isExerciseStart(line)) {
                break
            }
            bodyBuilder.append(line).append("\n")
            curr++
        }

        val material = AnalyzedMaterial(
            type = "EXERCISE",
            title = title,
            body = bodyBuilder.toString().trim(),
            sectionTitle = section,
            topicTitle = topic,
            isUncertain = false
        )

        return ParsedBlock(material, curr)
    }

    private fun parseTheoryBlock(lines: List<String>, startIndex: Int, section: String?, topic: String?): ParsedBlock {
        val bodyBuilder = StringBuilder(lines[startIndex])
        var curr = startIndex + 1

        while (curr < lines.size) {
            val line = lines[curr]
            if (isSectionHeader(line) || isTopicHeader(line) || isTestStart(line) || isExampleStart(line) || isExerciseStart(line) || isFormulaLine(line)) {
                break
            }
            bodyBuilder.append("\n").append(line)
            curr++
        }

        val material = AnalyzedMaterial(
            type = "THEORY",
            body = bodyBuilder.toString().trim(),
            sectionTitle = section,
            topicTitle = topic,
            isUncertain = false
        )

        return ParsedBlock(material, curr)
    }
}
