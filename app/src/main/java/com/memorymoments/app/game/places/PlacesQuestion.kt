package com.memorymoments.app.game.places

import com.memorymoments.app.model.Place

data class PlacesOption(
    val id: String,
    val placeId: String,
    val text: String,
    val isCorrect: Boolean
)

data class PlacesQuestion(
    val targetPlace: Place,
    val displayText: String,
    val options: List<PlacesOption>
)
