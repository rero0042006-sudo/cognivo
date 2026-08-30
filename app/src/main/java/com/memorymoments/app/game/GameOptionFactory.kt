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
        val usedUris = mutableSetOf<String>()
        val result = mutableListOf<GameCharacter>()

        // 1. Correct answer target
        val targetChar = GameCharacter.RealFamilyMember(target)
        result.add(targetChar)
        usedUris.add(targetChar.imageUri)

        // 2. Filter valid distractors (must have valid image files/URIs and not match target imageUri)
        val validDistractors = distractors.filter { d ->
            d.imageUri.isNotBlank() && !d.imageUri.contains("demo-") && d.imageUri !in usedUris &&
                (java.io.File(d.imageUri).exists() && java.io.File(d.imageUri).length() > 0)
        }.shuffled()

        // 3. Other family members with valid photo URIs
        val otherFamily = family.filter { f ->
            val photoUri = f.originalPhotoUri
            f.id != target.id && !photoUri.isNullOrBlank() && photoUri !in usedUris &&
                (java.io.File(photoUri).exists() && java.io.File(photoUri).length() > 0)
        }.shuffled()

        val needed = optionCount - 1

        // Combine available valid candidates
        val candidates = mutableListOf<GameCharacter>()
        for (d in validDistractors) {
            candidates.add(GameCharacter.AiDistractor(d))
        }
        for (f in otherFamily) {
            candidates.add(GameCharacter.RealFamilyMember(f))
        }

        // Shuffle candidates and pick needed count without URI duplicates
        for (candidate in candidates.shuffled()) {
            if (result.size >= optionCount) break
            if (candidate.imageUri !in usedUris) {
                result.add(candidate)
                usedUris.add(candidate.imageUri)
            }
        }

        return result.shuffled()
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
