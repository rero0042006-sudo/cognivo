package com.memorymoments.app.game

import com.memorymoments.app.model.AnswerOption
import com.memorymoments.app.model.DistractorCharacter
import com.memorymoments.app.model.FamilyMember
import com.memorymoments.app.model.GameCharacter

object GameOptionFactory {
    fun buildCharacters(
        target: FamilyMember,
        family: List<FamilyMember>,
        distractors: List<DistractorCharacter>,
        optionCount: Int = 4
    ): List<GameCharacter> {
        val others = family.filter { it.id != target.id }.shuffled()
        val needed = (optionCount - 1).coerceAtLeast(0)
        val initialExtraFamily = others.take((needed / 2).coerceAtMost(others.size))
        val neededDistractors = needed - initialExtraFamily.size
        val fakePeople = distractors.shuffled().take(neededDistractors)
        val stillNeeded = needed - (initialExtraFamily.size + fakePeople.size)
        val additionalFamily = if (stillNeeded > 0) {
            others.drop(initialExtraFamily.size).take(stillNeeded)
        } else {
            emptyList()
        }
        val allExtraFamily = initialExtraFamily + additionalFamily

        return (
            listOf(GameCharacter.RealFamilyMember(target)) +
                allExtraFamily.map { GameCharacter.RealFamilyMember(it) } +
                fakePeople.map { GameCharacter.AiDistractor(it) }
            ).shuffled()
    }

    fun toAnswerOptions(
        target: FamilyMember,
        characters: List<GameCharacter>
    ): List<AnswerOption> {
        return characters.map { character ->
            when (character) {
                is GameCharacter.RealFamilyMember -> AnswerOption(
                    id = character.id,
                    imageUri = character.imageUri,
                    isCorrect = character.familyMember.id == target.id,
                    familyMemberId = character.familyMember.id
                )
                is GameCharacter.AiDistractor -> AnswerOption(
                    id = character.id,
                    imageUri = character.imageUri,
                    isCorrect = false
                )
            }
        }
    }
}
