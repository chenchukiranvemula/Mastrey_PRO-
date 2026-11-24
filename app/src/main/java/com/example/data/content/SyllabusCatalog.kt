package com.example.data.content

import com.example.data.model.ChapterItem
import com.example.data.model.ExamSystem
import com.example.data.model.FlashcardItem
import com.example.data.model.FormulaItem
import com.example.data.model.QuestionItem
import com.example.data.model.SubjectItem

/**
 * Exam-isolated catalog router. Guarantees JEE, NEET UG, and CA Foundation
 * syllabi, formulas, questions, and flashcards never mix across ecosystems.
 */
object SyllabusCatalog {

    fun subjectsFor(exam: ExamSystem): List<SubjectItem> = when (exam) {
        ExamSystem.JEE_MAIN, ExamSystem.JEE_ADVANCED -> JeeContentProvider.subjects(exam)
        ExamSystem.NEET_UG -> NeetContentProvider.subjects()
        ExamSystem.CA_FOUNDATION -> CaContentProvider.subjects()
    }

    fun chaptersFor(exam: ExamSystem): List<ChapterItem> = when (exam) {
        ExamSystem.JEE_MAIN, ExamSystem.JEE_ADVANCED -> JeeContentProvider.chapters(exam)
        ExamSystem.NEET_UG -> NeetContentProvider.chapters()
        ExamSystem.CA_FOUNDATION -> CaContentProvider.chapters()
    }

    fun formulasFor(exam: ExamSystem): List<FormulaItem> = when (exam) {
        ExamSystem.JEE_MAIN, ExamSystem.JEE_ADVANCED -> JeeContentProvider.formulas(exam)
        ExamSystem.NEET_UG -> NeetContentProvider.formulas()
        ExamSystem.CA_FOUNDATION -> CaContentProvider.formulas()
    }

    fun allFormulas(): List<FormulaItem> =
        (JeeContentProvider.formulas(ExamSystem.JEE_MAIN) +
            NeetContentProvider.formulas() +
            CaContentProvider.formulas()).distinctBy { it.title + it.expression }

    fun questionsFor(exam: ExamSystem): List<QuestionItem> = when (exam) {
        ExamSystem.JEE_MAIN, ExamSystem.JEE_ADVANCED -> JeeContentProvider.questions(exam)
        ExamSystem.NEET_UG -> NeetContentProvider.questions()
        ExamSystem.CA_FOUNDATION -> CaContentProvider.questions()
    }

    fun flashcardsFor(exam: ExamSystem): List<FlashcardItem> = when (exam) {
        ExamSystem.JEE_MAIN, ExamSystem.JEE_ADVANCED -> JeeContentProvider.flashcards(exam)
        ExamSystem.NEET_UG -> NeetContentProvider.flashcards()
        ExamSystem.CA_FOUNDATION -> CaContentProvider.flashcards()
    }

    fun findChapter(exam: ExamSystem, chapterId: String): ChapterItem? =
        chaptersFor(exam).firstOrNull { it.id == chapterId }
}
