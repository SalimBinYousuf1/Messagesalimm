package com.example.data.telephony

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

object MediaAttachmentHelper {
    const val DEFAULT_MAX_MMS_SIZE_BYTES = 300 * 1024 // 300KB carrier safe default

    /**
     * Compresses an image at Uri to fit below the specified carrier MMS byte limit.
     * Uses dynamic scaling and JPEG quality adjustment.
     */
    suspend fun compressImageForMms(
        context: Context,
        imageUri: Uri,
        maxBytes: Int = DEFAULT_MAX_MMS_SIZE_BYTES
    ): File? = withContext(Dispatchers.IO) {
        try {
            val inputStream = context.contentResolver.openInputStream(imageUri) ?: return@withContext null
            val originalBitmap = BitmapFactory.decodeStream(inputStream)
            inputStream.close()

            if (originalBitmap == null) return@withContext null

            // Scale down if dimensions exceed 1280x1280
            val maxDim = 1280
            val width = originalBitmap.width
            val height = originalBitmap.height
            val scale = if (width > maxDim || height > maxDim) {
                val ratio = minOf(maxDim.toFloat() / width, maxDim.toFloat() / height)
                Bitmap.createScaledBitmap(
                    originalBitmap,
                    (width * ratio).toInt(),
                    (height * ratio).toInt(),
                    true
                )
            } else {
                originalBitmap
            }

            // Adjust quality iteratively to fit within maxBytes
            var quality = 85
            var stream = ByteArrayOutputStream()
            scale.compress(Bitmap.CompressFormat.JPEG, quality, stream)

            while (stream.size() > maxBytes && quality > 20) {
                quality -= 15
                stream.reset()
                scale.compress(Bitmap.CompressFormat.JPEG, quality, stream)
            }

            val outputFile = File(context.cacheDir, "mms_${System.currentTimeMillis()}.jpg")
            val fos = FileOutputStream(outputFile)
            fos.write(stream.toByteArray())
            fos.flush()
            fos.close()

            Log.d("MediaAttachmentHelper", "Compressed image from ${originalBitmap.byteCount} to ${outputFile.length()} bytes (target $maxBytes)")
            outputFile
        } catch (e: Exception) {
            Log.e("MediaAttachmentHelper", "Compression failed: ${e.message}", e)
            null
        }
    }
}
