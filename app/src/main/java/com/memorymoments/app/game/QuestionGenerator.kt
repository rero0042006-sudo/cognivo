package com.memorymoments.app.game

import com.memorymoments.app.model.DistractorCharacter
import com.memorymoments.app.model.FamilyMember
import com.memorymoments.app.model.Question

object QuestionGenerator {

    fun generateQuestion(
        target: FamilyMember,
        allFamily: List<FamilyMember>,
        distractors: List<DistractorCharacter>,
        optionCount: Int = 4
    ): Question {
        val questionText = formatQuestionText(target)
        val characters = GameOptionFactory.buildCharacters(
            target = target,
            family = allFamily,
            distractors = distractors,
            optionCount = optionCount
        )
        val options = GameOptionFactory.toAnswerOptions(target, characters)
        return Question(
            targetMember = target,
            text = questionText,
            options = options
        )
    }

    fun formatQuestionText(member: FamilyMember): String {
        val relationship = member.relationship.trim()
        val hasValidRelationship = relationship.isNotBlank() &&
            !relationship.equals("Other", ignoreCase = true)

        return if (hasValidRelationship) {
            "WHO IS YOUR ${relationship.uppercase()}?"
        } else {
            val displayName = member.nickname.ifBlank { member.name }.trim().uppercase()
            "WHO IS $displayName?"
        }
    }
}
