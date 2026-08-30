package com.memorymoments.app.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.net.Uri
import java.io.File

class ImageStorage(context: Context) {
    private val appContext = context.applicationContext

    private val photosDir: File
        get() = File(appContext.filesDir, "family_photos").also { it.mkdirs() }

    private val distractorsDir: File
        get() = File(appContext.filesDir, "distractors").also { it.mkdirs() }

    private val placesDir: File
        get() = File(appContext.filesDir, "place_photos").also { it.mkdirs() }

    private val memoriesDir: File
        get() = File(appContext.filesDir, "memory_photos").also { it.mkdirs() }

    fun copyMemoryPhoto(source: Uri, memoryId: String, photoIndex: Int = 0): Result<String> = runCatching {
        val resolver = appContext.contentResolver
        val mime = resolver.getType(source)?.lowercase().orEmpty()
        val extension = when {
            mime.contains("png") -> "png"
            mime.contains("webp") -> "webp"
            else -> "jpg"
        }
        val destination = File(memoriesDir, "${memoryId}_$photoIndex.$extension")
        resolver.openInputStream(source)?.use { input ->
            destination.outputStream().buffered().use { output ->
                input.copyTo(output, bufferSize = 8 * 1024)
            }
        } ?: error("unreadable")
        if (!destination.exists() || destination.length() == 0L) {
            error("empty")
        }
        destination.absolutePath
    }

    fun copyFromPicker(source: Uri, memberId: String): Result<String> = runCatching {
        val resolver = appContext.contentResolver
        val mime = resolver.getType(source)?.lowercase().orEmpty()
        val extension = when {
            mime.contains("png") -> "png"
            mime.contains("webp") -> "webp"
            else -> "jpg"
        }
        deleteOriginalForMember(memberId)
        val destination = File(photosDir, "$memberId.$extension")
        resolver.openInputStream(source)?.use { input ->
            destination.outputStream().buffered().use { output ->
                input.copyTo(output, bufferSize = 8 * 1024)
            }
        } ?: error("unreadable")
        if (!destination.exists() || destination.length() == 0L) {
            error("empty")
        }
        destination.absolutePath
    }

    fun copyPlacePhoto(source: Uri, placeId: String, photoIndex: Int = 0): Result<String> = runCatching {
        val resolver = appContext.contentResolver
        val mime = resolver.getType(source)?.lowercase().orEmpty()
        val extension = when {
            mime.contains("png") -> "png"
            mime.contains("webp") -> "webp"
            else -> "jpg"
        }
        val destination = File(placesDir, "${placeId}_$photoIndex.$extension")
        resolver.openInputStream(source)?.use { input ->
            destination.outputStream().buffered().use { output ->
                input.copyTo(output, bufferSize = 8 * 1024)
            }
        } ?: error("unreadable")
        if (!destination.exists() || destination.length() == 0L) {
            error("empty")
        }
        destination.absolutePath
    }

    fun saveDistractor(id: String, bytes: ByteArray, mimeType: String = "image/jpeg"): String {
        require(bytes.isNotEmpty()) { "empty-distractor" }
        val extension = extensionForMime(mimeType)
        val destination = File(distractorsDir, "$id.$extension")
        destination.outputStream().buffered().use { output ->
            output.write(bytes)
        }
        return destination.absolutePath
    }

    fun createDemoDistractor(id: String, index: Int): String {
        val size = 512
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val palette = demoColors[index % demoColors.size]
        canvas.drawColor(palette.background)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.color = palette.accent
        val cx = size / 2f
        canvas.drawCircle(cx, size * 0.38f, size * 0.18f, paint)
        canvas.drawCircle(cx, size * 0.82f, size * 0.32f, paint)
        paint.color = Color.WHITE
        paint.textAlign = Paint.Align.CENTER
        paint.textSize = 42f
        paint.isFakeBoldText = true
        canvas.drawText("P${index + 1}", cx, size * 0.4f, paint)
        val destination = File(distractorsDir, "$id.png")
        destination.outputStream().use { output ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, output)
        }
        bitmap.recycle()
        return destination.absolutePath
    }

    fun createDemoPlacePhoto(id: String, name: String, index: Int): String {
        val size = 512
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val palette = demoColors[index % demoColors.size]
        canvas.drawColor(palette.background)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.color = palette.accent
        val cx = size / 2f
        // Draw landscape/roof icon
        val path = android.graphics.Path().apply {
            moveTo(cx, size * 0.22f)
            lineTo(size * 0.85f, size * 0.52f)
            lineTo(size * 0.15f, size * 0.52f)
            close()
        }
        canvas.drawPath(path, paint)
        canvas.drawRect(size * 0.22f, size * 0.52f, size * 0.78f, size * 0.82f, paint)

        paint.color = Color.WHITE
        paint.textAlign = Paint.Align.CENTER
        paint.textSize = 34f
        paint.isFakeBoldText = true
        canvas.drawText(name.take(12), cx, size * 0.70f, paint)
        val destination = File(placesDir, "$id.png")
        destination.outputStream().use { output ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, output)
        }
        bitmap.recycle()
        return destination.absolutePath
    }

    fun deleteDistractor(path: String) {
        runCatching { File(path).takeIf { it.exists() }?.delete() }
    }

    fun clearAllDistractors() {
        distractorsDir.listFiles()?.forEach { it.delete() }
    }

    fun deleteForMember(memberId: String) {
        photosDir.listFiles()
            ?.filter { file ->
                file.nameWithoutExtension == memberId ||
                    file.name.startsWith("${memberId}_portrait")
            }
            ?.forEach { it.delete() }
    }

    fun deleteForPlace(placeId: String) {
        placesDir.listFiles()
            ?.filter { file ->
                file.nameWithoutExtension == placeId ||
                    file.nameWithoutExtension.startsWith("${placeId}_")
            }
            ?.forEach { it.delete() }
    }

    private fun deleteOriginalForMember(memberId: String) {
        photosDir.listFiles()
            ?.filter { it.nameWithoutExtension == memberId }
            ?.forEach { it.delete() }
    }

    companion object {
        private val demoColors = listOf(
            DemoPalette(0xFF2C2148.toInt(), 0xFF9B7AE8.toInt()),
            DemoPalette(0xFF14343A.toInt(), 0xFF5EB8C4.toInt()),
            DemoPalette(0xFF3A2A12.toInt(), 0xFFE2C15A.toInt()),
            DemoPalette(0xFF1E3328.toInt(), 0xFF5FBE86.toInt()),
            DemoPalette(0xFF3A1E24.toInt(), 0xFFD97A7A.toInt()),
            DemoPalette(0xFF332414.toInt(), 0xFFE39A5A.toInt()),
            DemoPalette(0xFF1A2740.toInt(), 0xFF7AA0E8.toInt()),
            DemoPalette(0xFF2A1A32.toInt(), 0xFFC47AE8.toInt())
        )

        fun loadModel(path: String?): Any? {
            if (path.isNullOrBlank()) return null
            val file = File(path)
            return if (file.isAbsolute) file else path
        }

        private fun extensionForMime(mimeType: String): String {
            val lower = mimeType.lowercase()
            return when {
                lower.contains("png") -> "png"
                lower.contains("webp") -> "webp"
                else -> "jpg"
            }
        }
    }

    private data class DemoPalette(val background: Int, val accent: Int)
}
