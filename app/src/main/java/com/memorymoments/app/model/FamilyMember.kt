package com.memorymoments.app.model

data class FamilyMember(
    val id: String,
    val name: String,
    val relationship: String,
    val nickname: String = "",
    val memoryContext: String = "",
    val originalPhotoUri: String? = null
) {
    val displayPhotoUri: String?
        get() = originalPhotoUri
}
