package com.memorymoments.app.game.places

import com.memorymoments.app.model.DistractorStyle
import com.memorymoments.app.model.Place
import java.io.File
import java.util.UUID

class PlacesGameEngine(
    private val places: List<Place>,
    private val difficulty: DistractorStyle = DistractorStyle.NORMAL
) {
    val usablePlaces: List<Place> = places.filter { place ->
        place.name.isNotBlank() &&
            place.photoUris.isNotEmpty() &&
            place.displayPhotoUri?.let { path -> File(path).exists() || path.startsWith("http") || path.startsWith("content") } == true
    }

    fun isReady(): Boolean = usablePlaces.size >= 3

    fun generateQuestions(count: Int = 10): List<PlacesQuestion> {
        if (!isReady()) return emptyList()

        val questions = mutableListOf<PlacesQuestion>()
        var lastTargetId: String? = null
        var pool = usablePlaces.shuffled().toMutableList()

        while (questions.size < count) {
            if (pool.isEmpty()) {
                pool = usablePlaces.shuffled().toMutableList()
            }

            // Pick next target avoiding consecutive duplicates
            var targetIndex = 0
            if (pool.size > 1 && pool[0].id == lastTargetId) {
                targetIndex = 1
            }
            val target = pool.removeAt(targetIndex)
            lastTargetId = target.id

            val optionCount = if (difficulty == DistractorStyle.EASY) 3 else 4
            val question = buildQuestion(target, usablePlaces, optionCount)
            questions.add(question)
        }

        return questions
    }

    private fun buildQuestion(
        target: Place,
        allPlaces: List<Place>,
        optionCount: Int
    ): PlacesQuestion {
        val useLocationAsAnswer = target.location?.isNotBlank() == true &&
            allPlaces.count { it.location?.isNotBlank() == true } >= optionCount

        val targetText = if (useLocationAsAnswer) target.location!!.trim() else target.name.trim()
        val questionPrompt = if (useLocationAsAnswer) {
            "WHERE WAS THIS?"
        } else {
            "WHICH PLACE IS THIS?"
        }

        // Gather distractors
        val otherPlaces = allPlaces.filter { it.id != target.id }

        // For Hard difficulty: prioritize places with similar/same location or date period if available
        val sortedOthers = if (difficulty == DistractorStyle.CHALLENGE) {
            otherPlaces.sortedByDescending { other ->
                var score = 0
                if (!target.location.isNullOrBlank() && other.location?.equals(target.location, ignoreCase = true) == true) score += 2
                if (!target.datePeriod.isNullOrBlank() && other.datePeriod?.equals(target.datePeriod, ignoreCase = true) == true) score += 1
                score
            }
        } else {
            otherPlaces.shuffled()
        }

        val neededDistractors = (optionCount - 1).coerceAtMost(sortedOthers.size)
        val selectedDistractorPlaces = sortedOthers.take(neededDistractors)

        val correctOption = PlacesOption(
            id = UUID.randomUUID().toString(),
            placeId = target.id,
            text = targetText,
            isCorrect = true
        )

        val distractorOptions = selectedDistractorPlaces.map { place ->
            val distractorText = if (useLocationAsAnswer && !place.location.isNullOrBlank()) {
                place.location!!.trim()
            } else {
                place.name.trim()
            }
            PlacesOption(
                id = UUID.randomUUID().toString(),
                placeId = place.id,
                text = if (distractorText.equals(targetText, ignoreCase = true)) "${place.name} (${place.location ?: ""})".trim() else distractorText,
                isCorrect = false
            )
        }

        val allOptions = (listOf(correctOption) + distractorOptions).shuffled()

        return PlacesQuestion(
            targetPlace = target,
            displayText = questionPrompt,
            options = allOptions
        )
    }
}
