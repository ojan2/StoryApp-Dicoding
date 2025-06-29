package com.application.storyapp.utils

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.os.Environment
import java.io.*
import java.text.SimpleDateFormat
import java.util.*

private const val FILENAME_FORMAT = "dd-MMM-yyyy"
private const val MAXIMAL_SIZE = 1000000 // 1 MB
private const val MAX_IMAGE_SIZE = 1024 // Max width/height in pixels

val timeStamp: String = SimpleDateFormat(
    FILENAME_FORMAT,
    Locale.US
).format(System.currentTimeMillis())

fun createCustomTempFile(context: Application): File {
    val storageDir: File? = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
    return File.createTempFile(timeStamp, ".jpg", storageDir)
}

fun uriToFile(selectedImg: Uri, context: Context): File {
    val contentResolver = context.contentResolver
    val myFile = createCustomTempFile(context.applicationContext as Application)

    val inputStream = contentResolver.openInputStream(selectedImg) as InputStream
    val outputStream: OutputStream = FileOutputStream(myFile)
    val buf = ByteArray(1024)
    var len: Int
    while (inputStream.read(buf).also { len = it } > 0) outputStream.write(buf, 0, len)
    outputStream.close()
    inputStream.close()

    return myFile
}

// FIXED VERSION - dengan proper bitmap sampling dan memory management
fun reduceFileImage(file: File): File {
    // Step 1: Get image dimensions without loading full bitmap
    val options = BitmapFactory.Options().apply {
        inJustDecodeBounds = true
    }
    BitmapFactory.decodeFile(file.path, options)

    // Step 2: Calculate sample size to reduce memory usage
    val sampleSize = calculateInSampleSize(options, MAX_IMAGE_SIZE, MAX_IMAGE_SIZE)

    // Step 3: Decode with sample size
    val finalOptions = BitmapFactory.Options().apply {
        inSampleSize = sampleSize
        inJustDecodeBounds = false
    }

    var bitmap: Bitmap? = null
    try {
        bitmap = BitmapFactory.decodeFile(file.path, finalOptions)

        // Check if bitmap is null or recycled
        if (bitmap == null || bitmap.isRecycled) {
            throw RuntimeException("Failed to decode bitmap or bitmap is recycled")
        }

        // Step 4: Handle rotation
        val rotatedBitmap = bitmap.getRotatedBitmap(file)

        // If rotation created a new bitmap, recycle the old one
        if (rotatedBitmap != bitmap) {
            bitmap.recycle()
            bitmap = rotatedBitmap
        }

        // Step 5: Compress with quality reduction
        var compressQuality = 100
        var outputStream: FileOutputStream? = null

        try {
            do {
                val bmpStream = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, compressQuality, bmpStream)
                val bmpPicByteArray = bmpStream.toByteArray()
                val streamLength = bmpPicByteArray.size

                if (streamLength <= MAXIMAL_SIZE) {
                    // Size is acceptable, write to file
                    outputStream = FileOutputStream(file)
                    outputStream.write(bmpPicByteArray)
                    break
                }

                compressQuality -= 5
                bmpStream.close()

            } while (compressQuality > 0)

        } finally {
            outputStream?.close()
        }

    } catch (e: Exception) {
        throw RuntimeException("Error processing image: ${e.message}", e)
    } finally {
        // IMPORTANT: Always recycle bitmap to free memory
        bitmap?.let {
            if (!it.isRecycled) {
                it.recycle()
            }
        }
    }

    return file
}

// Helper function to calculate appropriate sample size
private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
    val height = options.outHeight
    val width = options.outWidth
    var inSampleSize = 1

    if (height > reqHeight || width > reqWidth) {
        val halfHeight = height / 2
        val halfWidth = width / 2

        while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
            inSampleSize *= 2
        }
    }

    return inSampleSize
}

// FIXED VERSION - dengan safety checks
fun Bitmap.getRotatedBitmap(file: File): Bitmap {
    // Check if bitmap is recycled
    if (this.isRecycled) {
        throw RuntimeException("Cannot rotate recycled bitmap")
    }

    return try {
        val orientation = ExifInterface(file.absolutePath).getAttributeInt(
            ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED
        )
        when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> rotateImage(this, 90F)
            ExifInterface.ORIENTATION_ROTATE_180 -> rotateImage(this, 180F)
            ExifInterface.ORIENTATION_ROTATE_270 -> rotateImage(this, 270F)
            ExifInterface.ORIENTATION_NORMAL -> this
            else -> this
        }
    } catch (e: Exception) {
        // If rotation fails, return original bitmap
        this
    }
}

// FIXED VERSION - dengan safety checks
fun rotateImage(source: Bitmap, angle: Float): Bitmap {
    if (source.isRecycled) {
        throw RuntimeException("Cannot rotate recycled bitmap")
    }

    return try {
        val matrix = Matrix()
        matrix.postRotate(angle)
        val rotatedBitmap = Bitmap.createBitmap(
            source, 0, 0, source.width, source.height, matrix, true
        )

        // If createBitmap returned a new bitmap, we should recycle the original
        // Note: createBitmap sometimes returns the same bitmap if no transformation is needed
        if (rotatedBitmap != source) {
            // Only recycle if it's a different bitmap
            return rotatedBitmap
        }

        return source
    } catch (e: OutOfMemoryError) {
        throw RuntimeException("Out of memory while rotating bitmap", e)
    }
}