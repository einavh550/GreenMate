package com.example.greenmate_project.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

/**
 * Utility class for handling plant images.
 * Stores images locally in app's private storage to keep Firebase free.
 */
object ImageUtils {

    private const val PLANT_IMAGES_DIR = "plant_images"
    private const val IMAGE_QUALITY = 85
    private const val MAX_IMAGE_SIZE = 800 // Max width/height in pixels

    /**
     * Saves a bitmap image to local storage and returns the file path.
     * @param context Application context
     * @param bitmap The bitmap to save
     * @param existingPath Optional existing path to overwrite
     * @return The local file path of the saved image
     */
    fun saveImage(context: Context, bitmap: Bitmap, existingPath: String? = null): String {
        val imagesDir = getImagesDir(context)

        // Delete existing file if updating
        existingPath?.let { path ->
            val existingFile = File(path)
            if (existingFile.exists()) {
                existingFile.delete()
            }
        }

        // Generate unique filename
        val filename = "plant_${UUID.randomUUID()}.jpg"
        val file = File(imagesDir, filename)

        // Resize if needed
        val resizedBitmap = resizeIfNeeded(bitmap)

        // Save to file
        FileOutputStream(file).use { out ->
            resizedBitmap.compress(Bitmap.CompressFormat.JPEG, IMAGE_QUALITY, out)
        }

        return file.absolutePath
    }

    /**
     * Saves an image from URI to local storage.
     * @param context Application context
     * @param uri The content URI of the image
     * @param existingPath Optional existing path to overwrite
     * @return The local file path of the saved image, or null if failed
     */
    fun saveImageFromUri(context: Context, uri: Uri, existingPath: String? = null): String? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()

            bitmap?.let { saveImage(context, it, existingPath) }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Loads a bitmap from a local file path.
     * @param path The local file path
     * @return The bitmap, or null if file doesn't exist or can't be read
     */
    fun loadImage(path: String?): Bitmap? {
        if (path.isNullOrEmpty()) return null

        val file = File(path)
        if (!file.exists()) return null

        return try {
            BitmapFactory.decodeFile(path)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Deletes an image file.
     * @param path The local file path to delete
     */
    fun deleteImage(path: String?) {
        if (path.isNullOrEmpty()) return

        val file = File(path)
        if (file.exists()) {
            file.delete()
        }
    }

    /**
     * Checks if an image file exists at the given path.
     * @param path The local file path
     * @return True if the file exists
     */
    fun imageExists(path: String?): Boolean {
        if (path.isNullOrEmpty()) return false
        return File(path).exists()
    }

    /**
     * Gets or creates the images directory.
     */
    private fun getImagesDir(context: Context): File {
        val dir = File(context.filesDir, PLANT_IMAGES_DIR)
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir
    }

    /**
     * Resizes bitmap if larger than MAX_IMAGE_SIZE.
     */
    private fun resizeIfNeeded(bitmap: Bitmap): Bitmap {
        val width = bitmap.width
        val height = bitmap.height

        if (width <= MAX_IMAGE_SIZE && height <= MAX_IMAGE_SIZE) {
            return bitmap
        }

        val ratio = width.toFloat() / height.toFloat()
        val newWidth: Int
        val newHeight: Int

        if (width > height) {
            newWidth = MAX_IMAGE_SIZE
            newHeight = (MAX_IMAGE_SIZE / ratio).toInt()
        } else {
            newHeight = MAX_IMAGE_SIZE
            newWidth = (MAX_IMAGE_SIZE * ratio).toInt()
        }

        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
    }
}
