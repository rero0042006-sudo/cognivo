package com.memorymoments.app.data.remote

import com.google.gson.annotations.SerializedName

data class DistractorRequest(
    val difficulty: String,
    @SerializedName("visualAttributes")
    val visualAttributes: VisualAttributes? = null
)

/**
 * Broad visual attributes sent to the Cloudflare Worker for
 * Hard mode similar-character generation.
 * The Worker uses these to construct a controlled prompt.
 * The Android app NEVER sends the family photo to Cloudflare.
 */
data class VisualAttributes(
    val ageGroup: String,
    val hairColor: String,
    val hairStyle: String,
    val glasses: String,
    val clothing: String,
    val complexion: String,
    val generalBuild: String
)
