package com.example.data.ai

import android.util.Log
import com.example.data.model.AiResponseStyle
import com.example.data.model.ExamGroup
import com.example.data.model.ExamSystem
import com.example.data.model.QuestionDifficulty
import com.example.data.model.QuestionItem
import com.example.data.model.QuestionType
import com.example.data.model.VerificationStatus
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.Schema
import com.google.firebase.ai.type.content
import com.google.firebase.ai.type.generationConfig
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withTimeout
import org.json.JSONObject
import java.security.MessageDigest
import java.util.UUID

private const val AI_TAG = "MastreyAiService"
private const val MODEL_DEFAULT = "gemini-3.8-flash"

data class AiTutorContext(
    val exam: ExamSystem,
    val subjectName: String?,
    val chapterTitle: String?,
    val style: AiResponseStyle,
    val responseLength: String,
    val weakTopics: List<String>,
    val includeWeakTopicContext: Boolean
)

data class AiHistoryTurn(
    val isUser: Boolean,
    val text: String
)

interface MastreyAiProvider {
    fun streamTutorResponse(
        context: AiTutorContext,
        history: List<AiHistoryTurn>,
        userPrompt: String
    ): Flow<String>

    suspend fun generateValidatedQuestions(
        exam: ExamSystem,
        subjectId: String,
        subjectName: String,
        chapterId: String,
        chapterName: String,
        difficulty: QuestionDifficulty,
        count: Int,
        existingFingerprints: Set<String>
    ): Result<List<QuestionItem>>
}

class FirebaseMastreyAiService : MastreyAiProvider {

    private val chatModel by lazy {
        Firebase.ai.generativeModel(modelName = MODEL_DEFAULT)
    }

    private val questionGenSchema by lazy {
        Schema.obj(
            properties = mapOf(
                "questions" to Schema.array(
                    items = Schema.obj(
                        properties = mapOf(
                            "topicName" to Schema.string(description = "Specific topic inside the chapter"),
                            "questionText" to Schema.string(description = "Original exam-style question statement"),
                            "options" to Schema.array(
                                items = Schema.string(),
                                description = "Exactly 4 distinct answer choices without A/B/C/D prefix"
                            ),
                            "correctIndex" to Schema.integer(description = "0-based index of the single correct option (0..3)"),
                            "solutionSteps" to Schema.array(
                                items = Schema.string(),
                                description = "Step-by-step verified calculation or reasoning"
                            ),
                            "explanation" to Schema.string(description = "Concise concept explanation and exam tip"),
                            "relatedFormula" to Schema.string(description = "Primary formula or rule used"),
                            "selfCheckPassed" to Schema.boolean(description = "True only if arithmetic and concept consistency were verified")
                        )
                    )
                )
            )
        )
    }

    private val structuredQuestionModel by lazy {
        Firebase.ai.generativeModel(
            modelName = MODEL_DEFAULT,
            generationConfig = generationConfig {
                responseMimeType = "application/json"
                responseSchema = questionGenSchema
                temperature = 0.5f
            }
        )
    }

    private fun buildSystemInstruction(ctx: AiTutorContext): String {
        val profilePrompt = when (ctx.exam.group) {
            ExamGroup.JEE -> """
                You are MASTREY JEE AI, an expert educational tutor strictly for JEE Main & JEE Advanced (Physics, Chemistry, Mathematics).
                Focus on rigorous problem solving, numerical steps, derivations, exam shortcuts, and conceptual depth.
                NEVER mix NEET Biology or CA Foundation syllabus into JEE responses.
            """.trimIndent()
            ExamGroup.NEET -> """
                You are MASTREY NEET AI, an expert educational tutor strictly for NEET UG (NCERT Physics, Chemistry, Botany, Zoology).
                Emphasize NCERT terminology, medical entrance concepts, high-yield exceptions, and clear biological mechanisms.
                NEVER mix JEE Mathematics or CA Foundation content into NEET responses.
            """.trimIndent()
            ExamGroup.CA -> """
                You are MASTREY CA AI, an expert educational tutor strictly for ICAI CA Foundation (Accounting, Business Laws, Quantitative Aptitude, Business Economics).
                Use accurate accounting entries (Dr./Cr.), statutory definitions and case-law principles, financial math, and micro/macro economics clarity.
                NEVER mix JEE or NEET science content into CA Foundation responses.
            """.trimIndent()
        }

        val lengthGuide = when (ctx.responseLength) {
            "Concise" -> "Keep answers compact and direct (under 180 words unless solving multi-step math)."
            "Comprehensive" -> "Provide thorough explanations with worked examples and common mistake warnings."
            else -> "Provide balanced clarity with structured bullet points and key formulas."
        }

        val weakContext = if (ctx.includeWeakTopicContext && ctx.weakTopics.isNotEmpty()) {
            "\nStudent weak topics to reinforce gently if relevant: ${ctx.weakTopics.take(5).joinToString(", ")}."
        } else ""

        val subjectScope = buildString {
            if (!ctx.subjectName.isNullOrBlank()) append("\nActive Subject: ${ctx.subjectName}.")
            if (!ctx.chapterTitle.isNullOrBlank()) append("\nActive Chapter: ${ctx.chapterTitle}.")
        }

        return """
            $profilePrompt
            Active Exam: ${ctx.exam.title}.$subjectScope
            Explanation Style: ${ctx.style.label} — ${ctx.style.instruction}
            Length Preference: $lengthGuide$weakContext
            Accuracy Policy: Verify calculations carefully. Never fabricate official previous-year question years or official college cutoffs. If unsure about a fact, state uncertainty clearly.
            Use clean readable mathematical notation (Unicode symbols like √, ², ³, π, θ, Δ, →, ∫, Σ) without raw LaTeX dollar signs.
        """.trimIndent()
    }

    override fun streamTutorResponse(
        context: AiTutorContext,
        history: List<AiHistoryTurn>,
        userPrompt: String
    ): Flow<String> = flow {
        val systemBlock = buildSystemInstruction(context)
        val conversationPrompt = buildString {
            append(systemBlock)
            append("\n\n--- Conversation ---\n")
            history.takeLast(8).forEach { turn ->
                append(if (turn.isUser) "Student: " else "MASTREY AI: ")
                append(turn.text.take(1500))
                append("\n\n")
            }
            append("Student: ")
            append(userPrompt)
            append("\n\nMASTREY AI:")
        }

        val accumulated = StringBuilder()
        try {
            withTimeout(60_000L) {
                chatModel.generateContentStream(content { text(conversationPrompt) }).collect { chunk ->
                    val piece = chunk.text.orEmpty()
                    if (piece.isNotEmpty()) {
                        accumulated.append(piece)
                        emit(accumulated.toString())
                    }
                }
            }
            if (accumulated.isBlank()) {
                throw IllegalStateException("MASTREY AI returned an empty response. Please retry.")
            }
        } catch (e: TimeoutCancellationException) {
            Log.e(AI_TAG, "Streaming tutor timed out", e)
            throw IllegalStateException("Response timed out after 60 seconds. Your prompt is preserved—please retry.")
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(AI_TAG, "Streaming tutor failed", e)
            throw e
        }
    }

    override suspend fun generateValidatedQuestions(
        exam: ExamSystem,
        subjectId: String,
        subjectName: String,
        chapterId: String,
        chapterName: String,
        difficulty: QuestionDifficulty,
        count: Int,
        existingFingerprints: Set<String>
    ): Result<List<QuestionItem>> {
        val requestedCount = count.coerceIn(1, 5)
        val prompt = """
            Generate $requestedCount ORIGINAL, conceptually distinct ${difficulty.label}-difficulty practice MCQs for:
            Exam: ${exam.title} (${exam.patternDescription})
            Subject: $subjectName
            Chapter: $chapterName

            STRICT RULES:
            1. Every question MUST belong strictly to ${exam.title} -> $subjectName -> $chapterName.
            2. Do NOT claim any question is an official previous-year question.
            3. Provide exactly 4 unique, non-overlapping options and verify the math/science/law accuracy so correctIndex strictly matches solutionSteps.
            4. Avoid trivial number swaps; test different conceptual angles.
            5. Use clean Unicode math symbols (√, ², ³, π, θ, Δ, →) rather than raw LaTeX delimiters.
            6. Set selfCheckPassed = true only after verifying the final option matches the solved result.
        """.trimIndent()

        return try {
            val response = withTimeout(60_000L) {
                structuredQuestionModel.generateContent(prompt)
            }
            val jsonText = response.text ?: throw IllegalStateException("Empty AI question response.")
            val root = JSONObject(jsonText)
            val arr = root.getJSONArray("questions")
            val validated = mutableListOf<QuestionItem>()
            val seenBatchFingerprints = existingFingerprints.toMutableSet()

            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                val qText = obj.optString("questionText").trim()
                val optionsArr = obj.optJSONArray("options") ?: continue
                val options = (0 until optionsArr.length()).map { optionsArr.getString(it).trim() }.filter { it.isNotBlank() }
                val correctIdx = obj.optInt("correctIndex", -1)
                val stepsArr = obj.optJSONArray("solutionSteps")
                val steps = if (stepsArr != null) {
                    (0 until stepsArr.length()).map { stepsArr.getString(it).trim() }.filter { it.isNotBlank() }
                } else emptyList()
                val selfCheck = obj.optBoolean("selfCheckPassed", false)

                // Multi-stage validation pipeline: format, uniqueness, option bounds, self-check
                if (!selfCheck || qText.length < 15 || options.size != 4 || options.distinct().size != 4) continue
                if (correctIdx !in 0..3 || steps.isEmpty()) continue

                val fingerprint = computeQuestionFingerprint(qText)
                if (fingerprint in seenBatchFingerprints) continue
                seenBatchFingerprints.add(fingerprint)

                val topicName = obj.optString("topicName").trim().ifBlank { chapterName }
                validated.add(
                    QuestionItem(
                        id = "ai_${UUID.randomUUID().toString().replace("-", "").take(12)}",
                        fingerprint = fingerprint,
                        examId = exam.id,
                        subjectId = subjectId,
                        subjectName = subjectName,
                        chapterId = chapterId,
                        chapterName = chapterName,
                        topicId = "${chapterId}_ai",
                        topicName = topicName,
                        difficulty = difficulty,
                        questionType = QuestionType.SINGLE_CHOICE,
                        questionText = qText,
                        options = options,
                        correctAnswer = options[correctIdx],
                        solutionSteps = steps,
                        explanation = obj.optString("explanation").trim().ifBlank { "AI-generated solution validated for format consistency." },
                        relatedFormula = obj.optString("relatedFormula").trim().ifBlank { "Core concept from $chapterName" },
                        sourceLabel = "AI-Generated Original (Validated)",
                        verificationStatus = VerificationStatus.AI_GENERATED_VALIDATED
                    )
                )
            }

            if (validated.isEmpty()) {
                Result.failure(IllegalStateException("Generated questions did not pass validation checks. Please try again."))
            } else {
                Result.success(validated)
            }
        } catch (e: TimeoutCancellationException) {
            Log.e(AI_TAG, "AI question generation timed out", e)
            Result.failure(IllegalStateException("Question generation timed out after 60 seconds. Please retry."))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(AI_TAG, "AI question generation failed", e)
            Result.failure(e)
        }
    }

    companion object {
        fun computeQuestionFingerprint(text: String): String {
            val normalized = text.lowercase()
                .replace(Regex("[^a-z0-9 ]"), " ")
                .replace(Regex("\\s+"), " ")
                .trim()
            val digest = MessageDigest.getInstance("SHA-256").digest(normalized.toByteArray())
            return digest.take(12).joinToString("") { "%02x".format(it) }
        }
    }
}
