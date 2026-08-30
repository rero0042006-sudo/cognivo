package com.memorymoments.app.game

import com.memorymoments.app.model.DistractorCharacter
import com.memorymoments.app.model.DistractorStyle
import com.memorymoments.app.model.FamilyMember
import com.memorymoments.app.model.Question
import com.memorymoments.app.utils.Constants

class GameEngine(
    private val family: List<FamilyMember>,
    private val distractors: List<DistractorCharacter>,
    private val config: GameConfig = GameConfig(),
    private val style: DistractorStyle = DistractorStyle.NORMAL
) {

    fun isReady(): Boolean = family.size >= Constants.MIN_FAMILY_FOR_GAME

    fun generateQuestions(count: Int = config.roundCount): List<Question> {
        if (family.isEmpty()) return emptyList()

        val questions = mutableListOf<Question>()
        var pool = family.shuffled().toMutableList()
        val optionCount = when (style) {
            DistractorStyle.EASY -> 2
            DistractorStyle.NORMAL -> 4
            DistractorStyle.CHALLENGE -> 6
        }

        while (questions.size < count) {
            if (pool.isEmpty()) {
                pool = family.shuffled().toMutableList()
            }
            val target = pool.removeAt(0)
            val memberDistractors = distractors.filter { it.sourceFamilyMemberId == target.id }
            val selectedDistractors = if (memberDistractors.isNotEmpty()) memberDistractors else distractors

            val question = QuestionGenerator.generateQuestion(
                target = target,
                allFamily = family,
                distractors = selectedDistractors,
                optionCount = optionCount
            )
            questions.add(question)
        }

        return questions
    }
}
