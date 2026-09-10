# Math MASTER — Problem Data Import Architecture

This guide explains how to import and scale mathematical problem datasets (up to 20,620+ questions) into **Math MASTER**.

---

## 1. Overview
Math MASTER uses an offline-first **Room Database** with SQLite indexing on `questionHash`, `topicId`, and `category`.
The `ProblemImporter` class (`com.example.data.importer.ProblemImporter`) validates, dedupes, and batch-inserts mathematical questions into the `problems` table without overwriting or generating collisions.

---

## 2. JSON Schema Specification

The dataset should be structured as a JSON Array of objects:

```json
[
  {
    "id": "prob_alg_linear_001",
    "category": "Algebra",
    "topic": "alg_linear",
    "difficulty": "MEDIUM",
    "question": "Solve for x: 3x - 5 = 2x + 7",
    "options": [
      "x = 12",
      "x = 2",
      "x = -12",
      "x = 6"
    ],
    "answer": "A",
    "solution": "3x - 2x = 7 + 5 => x = 12.",
    "explanation": "Isolate the linear variable term by subtracting 2x and adding 5.",
    "formula": "ax + b = cx + d",
    "tags": "linear,algebra,equations",
    "question_hash": "optional_precomputed_sha256_hash",
    "generator": "MathMaster-CoreGen"
  }
]
```

### Supported Field Formats:
- **`options`**: Can either be a JSON array of 4 strings `["A text", "B text", "C text", "D text"]`, or discrete fields `"optionA"`, `"optionB"`, `"optionC"`, `"optionD"`.
- **`answer` / `correctAnswer`**: Can be `"A"`, `"B"`, `"C"`, or `"D"`.
- **`difficulty`**: `"EASY"`, `"MEDIUM"`, `"HARD"`, or `"EXPERT"`.
- **`question_hash`**: If omitted or empty, `ProblemImporter` automatically computes a deterministic SHA-256 hash of `(question + optionA + optionB + optionC + optionD)`.

---

## 3. Supported Mathematical Categories

Math MASTER is pre-configured with 15 standard and advanced categories:
1. **Algebra** (Linear, Quadratics, Polynomials, Systems)
2. **Trigonometry** (Identities, Equations, Unit Circle)
3. **Geometry** (Triangles, Circles, Polygons, Solid Geometry)
4. **Number Theory** (Divisibility, Primes, Modular Arithmetic, Diophantine)
5. **Probability** (Classical, Conditional, Distributions)
6. **Combinatorics** (Permutations, Combinations, Pigeonhole)
7. **Statistics** (Descriptive, Variance, Distributions)
8. **Calculus** (Limits, Derivatives, Integrals)
9. **Logic** (Boolean, Set Theory, Quantifiers)
10. **Olympiad** (Advanced Inequalities, Non-standard problem solving)
11. **MAXSUS** (Specialized book content and advanced competitions)

---

## 4. How to Import Programmatically

```kotlin
val importer = appContainer.problemImporter

// Read raw JSON string (from assets, file storage, or remote sync)
val jsonString = context.assets.open("math_master_dataset.json").bufferedReader().use { it.readText() }

// Execute validation & batch import
val result: ImportResult = importer.importFromJson(jsonString)

println("Total Processed: ${result.totalProcessed}")
println("Successfully Inserted: ${result.insertedCount}")
println("Duplicate Skipped: ${result.duplicateCount}")
println("Malformed Skipped: ${result.malformedCount}")
```

### Safety Features:
1. **Deduplication:** Questions matching an existing `questionHash` are safely skipped via SQLite `IGNORE`.
2. **Validation:** Incomplete entries missing question text or options are safely rejected without aborting the batch.
3. **Performance:** Batch insertions run in a single coroutine transaction on `Dispatchers.IO`.
