package com.memorymoments.app.game

import com.memorymoments.app.model.DistractorCharacter
import com.memorymoments.app.model.FamilyMember
import com.memorymoments.app.model.Question
import com.memorymoments.app.utils.Constants

class GameEngine(
    private val family: List<FamilyMember>,
    private val distractors: List<DistractorCharacter>,
    private val config: GameConfig = GameConfig()
) {

    fun isReady(): Boolean = family.size >= Constants.MIN_FAMILY_FOR_GAME

    fun generateQuestions(count: Int = config.roundCount): List<Question> {
        if (family.isEmpty()) return emptyList()

        val questions = mutableListOf<Question>()
        var pool = family.shuffled().toMutableList()

        while (questions.size < count) {
            if (pool.isEmpty()) {
                pool = family.shuffled().toMutableList()
            }
            val target = pool.removeAt(0)
            val question = QuestionGenerator.generateQuestion(
                target = target,
                allFamily = family,
                distractors = distractors,
                optionCount = 4
            )
            questions.add(question)
        }

        return questions
    }
}
