package com.memorymoments.app.model

import com.google.gson.annotations.SerializedName

/**
 * Broad visual characteristics extracted from a family member's photo
 * by Groq Vision for Hard mode distractor generation.
 *
 * These are non-identifying attributes used to generate fictional
 * characters with similar broad appearance. No biometric data,
 * face embeddings, or identity information is stored.
 */
data class VisualProfile(
    val familyMemberId: String,
    val ageGroup: AgeGroup = AgeGroup.UNKNOWN,
    val hairColor: HairColor = HairColor.UNKNOWN,
    val hairStyle: HairStyle = HairStyle.UNKNOWN,
    val glasses: GlassesOption = GlassesOption.UNKNOWN,
    val clothing: ClothingStyle = ClothingStyle.UNKNOWN,
    val complexion: Complexion = Complexion.UNKNOWN,
    val generalBuild: GeneralBuild = GeneralBuild.UNKNOWN,
    val createdAt: Long = System.currentTimeMillis()
) {
    fun toAttributesMap(): Map<String, String> {
        return mapOf(
            "ageGroup" to ageGroup.apiValue,
            "hairColor" to hairColor.apiValue,
            "hairStyle" to hairStyle.apiValue,
            "glasses" to when (glasses) {
                GlassesOption.YES -> "true"
                GlassesOption.NO -> "false"
                GlassesOption.UNKNOWN -> "unknown"
            },
            "clothing" to clothing.apiValue,
            "complexion" to complexion.apiValue,
            "generalBuild" to generalBuild.apiValue
        )
    }
}

enum class AgeGroup(val apiValue: String) {
    @SerializedName("child") CHILD("child"),
    @SerializedName("young_adult") YOUNG_ADULT("young_adult"),
    @SerializedName("adult") ADULT("adult"),
    @SerializedName("middle_aged") MIDDLE_AGED("middle_aged"),
    @SerializedName("older_adult") OLDER_ADULT("older_adult"),
    @SerializedName("unknown") UNKNOWN("unknown");

    companion object {
        fun fromString(s: String?): AgeGroup =
            entries.firstOrNull { it.apiValue.equals(s, ignoreCase = true) } ?: UNKNOWN
    }
}

enum class HairColor(val apiValue: String) {
    @SerializedName("black") BLACK("black"),
    @SerializedName("brown") BROWN("brown"),
    @SerializedName("blonde") BLONDE("blonde"),
    @SerializedName("red") RED("red"),
    @SerializedName("gray") GRAY("gray"),
    @SerializedName("white") WHITE("white"),
    @SerializedName("mixed") MIXED("mixed"),
    @SerializedName("unknown") UNKNOWN("unknown");

    companion object {
        fun fromString(s: String?): HairColor =
            entries.firstOrNull { it.apiValue.equals(s, ignoreCase = true) } ?: UNKNOWN
    }
}

enum class HairStyle(val apiValue: String) {
    @SerializedName("short") SHORT("short"),
    @SerializedName("medium") MEDIUM("medium"),
    @SerializedName("long") LONG("long"),
    @SerializedName("curly") CURLY("curly"),
    @SerializedName("straight") STRAIGHT("straight"),
    @SerializedName("wavy") WAVY("wavy"),
    @SerializedName("bald") BALD("bald"),
    @SerializedName("receding") RECEDING("receding"),
    @SerializedName("unknown") UNKNOWN("unknown");

    companion object {
        fun fromString(s: String?): HairStyle =
            entries.firstOrNull { it.apiValue.equals(s, ignoreCase = true) } ?: UNKNOWN
    }
}

enum class GlassesOption(val apiValue: String) {
    @SerializedName("true") YES("true"),
    @SerializedName("false") NO("false"),
    @SerializedName("unknown") UNKNOWN("unknown");

    companion object {
        fun fromString(s: String?): GlassesOption = when {
            s.equals("true", ignoreCase = true) -> YES
            s.equals("false", ignoreCase = true) -> NO
            else -> UNKNOWN
        }
    }
}

enum class ClothingStyle(val apiValue: String) {
    @SerializedName("casual") CASUAL("casual"),
    @SerializedName("formal") FORMAL("formal"),
    @SerializedName("traditional") TRADITIONAL("traditional"),
    @SerializedName("sportswear") SPORTSWEAR("sportswear"),
    @SerializedName("unknown") UNKNOWN("unknown");

    companion object {
        fun fromString(s: String?): ClothingStyle =
            entries.firstOrNull { it.apiValue.equals(s, ignoreCase = true) } ?: UNKNOWN
    }
}

enum class Complexion(val apiValue: String) {
    @SerializedName("light") LIGHT("light"),
    @SerializedName("medium") MEDIUM("medium"),
    @SerializedName("deep") DEEP("deep"),
    @SerializedName("unknown") UNKNOWN("unknown");

    companion object {
        fun fromString(s: String?): Complexion =
            entries.firstOrNull { it.apiValue.equals(s, ignoreCase = true) } ?: UNKNOWN
    }
}

enum class GeneralBuild(val apiValue: String) {
    @SerializedName("slim") SLIM("slim"),
    @SerializedName("average") AVERAGE("average"),
    @SerializedName("broad") BROAD("broad"),
    @SerializedName("unknown") UNKNOWN("unknown");

    companion object {
        fun fromString(s: String?): GeneralBuild =
            entries.firstOrNull { it.apiValue.equals(s, ignoreCase = true) } ?: UNKNOWN
    }
}
