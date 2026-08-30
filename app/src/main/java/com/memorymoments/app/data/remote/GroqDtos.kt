package com.memorymoments.app.data.remote

import com.google.gson.annotations.SerializedName

data class GroqChatRequest(
    val model: String = "openai/gpt-oss-120b",
    val messages: List<GroqChatMessage>,
    val temperature: Double = 0.3,
    @SerializedName("max_tokens")
    val maxTokens: Int = 300
)

/**
 * Supports both text-only and multimodal (vision) messages.
 * For text-only: use [content] as a string, leave [multiContent] null.
 * For vision: use [multiContent] as a list of content parts, leave [content] null.
 */
data class GroqChatMessage(
    val role: String,
    val content: Any? = null
)

/**
 * A content part for multimodal (vision) messages.
 * For text: type = "text", text = "...", imageUrl = null.
 * For image: type = "image_url", text = null, imageUrl = ImageUrlPart(...).
 */
data class GroqContentPart(
    val type: String,
    val text: String? = null,
    @SerializedName("image_url")
    val imageUrl: ImageUrlPart? = null
)

data class ImageUrlPart(
    val url: String
)

data class GroqChatResponse(
    val id: String?,
    val choices: List<GroqChoice>?
)

data class GroqChoice(
    val index: Int?,
    val message: GroqResponseMessage?
)

data class GroqResponseMessage(
    val role: String?,
    val content: String?,
    val reasoning: String?
)
