package com.example.data.model

enum class ExamGroup(val displayName: String, val aiTitle: String, val aiSubtitle: String) {
    JEE("JEE", "MASTREY JEE AI", "Physics • Chemistry • Mathematics • Advanced Problem Solving"),
    NEET("NEET UG", "MASTREY NEET AI", "NCERT Physics • Chemistry • Botany • Zoology"),
    CA("CA Foundation", "MASTREY CA AI", "Accounting • Business Laws • Quantitative Aptitude • Economics")
}

enum class ExamSystem(
    val id: String,
    val title: String,
    val shortTitle: String,
    val group: ExamGroup,
    val positiveMarks: Double,
    val negativeMarks: Double,
    val defaultDurationMinutes: Int,
    val maxExamMarks: Int,
    val patternDescription: String,
    val syllabusVersion: String
) {
    JEE_MAIN(
        id = "JEE_MAIN",
        title = "JEE Main",
        shortTitle = "JEE Main",
        group = ExamGroup.JEE,
        positiveMarks = 4.0,
        negativeMarks = 1.0,
        defaultDurationMinutes = 180,
        maxExamMarks = 300,
        patternDescription = "Physics, Chemistry & Mathematics • +4 / -1 MCQ & Numerical Pattern",
        syllabusVersion = "NTA JEE Main Syllabus Reference"
    ),
    JEE_ADVANCED(
        id = "JEE_ADVANCED",
        title = "JEE Advanced",
        shortTitle = "JEE Adv",
        group = ExamGroup.JEE,
        positiveMarks = 4.0,
        negativeMarks = 2.0,
        defaultDurationMinutes = 180,
        maxExamMarks = 360,
        patternDescription = "Multi-concept Physics, Chemistry & Math • Single, Multi-Correct & Integer",
        syllabusVersion = "JEE Advanced Official Syllabus Reference"
    ),
    NEET_UG(
        id = "NEET_UG",
        title = "NEET UG",
        shortTitle = "NEET UG",
        group = ExamGroup.NEET,
        positiveMarks = 4.0,
        negativeMarks = 1.0,
        defaultDurationMinutes = 180,
        maxExamMarks = 720,
        patternDescription = "NCERT-Focused Physics, Chemistry, Botany & Zoology • +4 / -1",
        syllabusVersion = "NMC / NCERT NEET UG Syllabus Reference"
    ),
    CA_FOUNDATION(
        id = "CA_FOUNDATION",
        title = "CA Foundation",
        shortTitle = "CA Found.",
        group = ExamGroup.CA,
        positiveMarks = 1.0,
        negativeMarks = 0.25,
        defaultDurationMinutes = 120,
        maxExamMarks = 400,
        patternDescription = "Accounting, Business Laws, Quantitative Aptitude & Economics",
        syllabusVersion = "ICAI New Scheme Syllabus Reference"
    );

    companion object {
        fun fromId(raw: String?): ExamSystem =
            entries.firstOrNull { it.id == raw || it.name == raw } ?: JEE_MAIN
    }
}

enum class PrepLevel(val label: String) {
    BEGINNER("Beginner"),
    INTERMEDIATE("Intermediate"),
    ADVANCED("Advanced");

    companion object {
        fun fromName(raw: String?): PrepLevel =
            entries.firstOrNull { it.name == raw } ?: INTERMEDIATE
    }
}

enum class ExplanationDepth(val label: String, val subtitle: String) {
    BEGINNER("Beginner", "From absolute basics"),
    STANDARD("Standard", "Exam-level clarity"),
    ADVANCED("Advanced", "Deep conceptual mastery")
}

enum class VerificationStatus(val badgeText: String) {
    VERIFIED_EDUCATIONAL("Verified Educational Content"),
    AI_GENERATED_VALIDATED("AI-Generated • Validated"),
    AWAITING_VERIFIED("Awaiting Verified Content")
}

enum class QuestionDifficulty(val label: String) {
    EASY("Easy"),
    MEDIUM("Medium"),
    HARD("Hard");

    companion object {
        fun fromName(raw: String?): QuestionDifficulty =
            entries.firstOrNull { it.name == raw } ?: MEDIUM
    }
}

enum class QuestionType(val label: String) {
    SINGLE_CHOICE("Single Choice MCQ"),
    NUMERICAL("Numerical / Integer"),
    ASSERTION_REASON("Assertion & Reason"),
    CASE_BASED("Case-Based"),
    MULTI_CONCEPT("Multi-Concept")
}

enum class RevisionConfidence(val label: String, val nextIntervalHours: Long) {
    EASY("Easy", 168L),
    NEED_REVISION("Need Revision", 48L),
    DIFFICULT("Difficult", 12L);

    companion object {
        fun fromName(raw: String?): RevisionConfidence =
            entries.firstOrNull { it.name == raw } ?: NEED_REVISION
    }
}

enum class BookmarkType(val label: String) {
    FORMULA("Formula"),
    NOTE("Note"),
    TOPIC("Topic"),
    QUESTION("Question"),
    AI_ANSWER("AI Answer"),
    FLASHCARD("Flashcard");

    companion object {
        fun fromName(raw: String?): BookmarkType =
            entries.firstOrNull { it.name == raw } ?: TOPIC
    }
}

enum class FlashcardCategory(val label: String) {
    FORMULA("Formula"),
    DEFINITION("Definition"),
    CONCEPT("Concept"),
    REACTION("Reaction"),
    LAW("Law"),
    ACCOUNTING_ENTRY("Accounting Entry")
}

enum class AiResponseStyle(val label: String, val instruction: String) {
    SIMPLE("Simple", "Use simple everyday analogies and plain beginner-friendly language."),
    STEP_BY_STEP("Step-by-step", "Break the explanation into clear numbered steps with intermediate results."),
    SHORT("Short", "Give a crisp, concise answer focusing only on the key takeaway and formula."),
    DETAILED("Detailed", "Provide an in-depth explanation including conceptual intuition, edge cases, and examples."),
    EXAM_FOCUSED("Exam-focused", "Focus on exam-relevant traps, high-yield points, and time-saving solving methods."),
    ADVANCED("Advanced", "Use rigorous formal reasoning, deeper derivations, and challenging cross-topic insights.")
}

data class SubjectItem(
    val id: String,
    val examId: String,
    val name: String,
    val subtitle: String,
    val iconName: String,
    val weightageNote: String
)

data class SolvedExample(
    val problem: String,
    val steps: List<String>,
    val finalAnswer: String
)

data class TopicItem(
    val id: String,
    val chapterId: String,
    val title: String,
    val subtopics: List<String>,
    val beginnerTheory: String,
    val standardTheory: String,
    val advancedTheory: String,
    val keyDefinitions: List<String>,
    val derivationNote: String?,
    val solvedExamples: List<SolvedExample>,
    val shortcuts: List<String>,
    val commonMistakes: List<String>,
    val examTips: List<String>,
    val revisionPoints: List<String>,
    val verificationStatus: VerificationStatus = VerificationStatus.VERIFIED_EDUCATIONAL
)

data class ChapterItem(
    val id: String,
    val subjectId: String,
    val examId: String,
    val order: Int,
    val title: String,
    val introduction: String,
    val learningObjectives: List<String>,
    val importanceTag: String,
    val estimatedMinutes: Int,
    val topics: List<TopicItem>,
    val verificationStatus: VerificationStatus = VerificationStatus.VERIFIED_EDUCATIONAL
)

data class FormulaItem(
    val id: String,
    val examId: String,
    val subjectId: String,
    val subjectName: String,
    val chapterId: String,
    val chapterName: String,
    val topicName: String,
    val category: String,
    val title: String,
    val expression: String,
    val variables: Map<String, String>,
    val units: String,
    val explanation: String,
    val conditionsOfUse: String,
    val example: String,
    val relatedFormulaIds: List<String> = emptyList(),
    val keywords: List<String> = emptyList(),
    val importanceLevel: String = "High Yield",
    val verificationStatus: VerificationStatus = VerificationStatus.VERIFIED_EDUCATIONAL
)

data class QuestionItem(
    val id: String,
    val fingerprint: String,
    val examId: String,
    val subjectId: String,
    val subjectName: String,
    val chapterId: String,
    val chapterName: String,
    val topicId: String,
    val topicName: String,
    val difficulty: QuestionDifficulty,
    val questionType: QuestionType,
    val questionText: String,
    val options: List<String>,
    val correctAnswer: String,
    val solutionSteps: List<String>,
    val explanation: String,
    val relatedFormula: String,
    val sourceLabel: String = "Verified Practice Bank",
    val verificationStatus: VerificationStatus = VerificationStatus.VERIFIED_EDUCATIONAL
)

data class FlashcardItem(
    val id: String,
    val examId: String,
    val subjectId: String,
    val subjectName: String,
    val chapterId: String,
    val chapterName: String,
    val category: FlashcardCategory,
    val difficulty: QuestionDifficulty,
    val front: String,
    val back: String,
    val hint: String
)

data class AchievementItem(
    val id: String,
    val title: String,
    val description: String,
    val unlocked: Boolean,
    val progressText: String
)
