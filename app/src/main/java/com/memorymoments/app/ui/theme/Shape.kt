package com.memorymoments.app.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Immutable
import androidx.compose.ui.unit.dp

@Immutable
data class MmShapes(
    val button: RoundedCornerShape = RoundedCornerShape(16.dp),
    val panel: RoundedCornerShape = RoundedCornerShape(22.dp),
    val card: RoundedCornerShape = RoundedCornerShape(20.dp),
    val badge: RoundedCornerShape = RoundedCornerShape(12.dp),
    val bar: RoundedCornerShape = RoundedCornerShape(8.dp),
    val pill: RoundedCornerShape = RoundedCornerShape(9999.dp)
)
