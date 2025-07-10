@file:Suppress("UNREACHABLE_CODE", "SameParameterValue", "SameParameterValue")

package com.application.storyapp.utils

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix

import android.net.Uri
import android.os.Environment
import androidx.exifinterface.media.ExifInterface
import java.io.*
import java.text.SimpleDateFormat
import java.util.*

private const val FILENAME_FORMAT = "dd-MMM-yyyy"
private const val MAXIMAL_SIZE = 1000000
private const val MAX_IMAGE_SIZE = 1024

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

fun reduceFileImage(file: File): File {

    val options = BitmapFactory.Options().apply {
        inJustDecodeBounds = true
    }
    BitmapFactory.decodeFile(file.path, options)

    val sampleSize = calculateInSampleSize(options, MAX_IMAGE_SIZE, MAX_IMAGE_SIZE)

    val finalOptions = BitmapFactory.Options().apply {
        inSampleSize = sampleSize
        inJustDecodeBounds = false
    }

    var bitmap: Bitmap? = null
    try {
        bitmap = BitmapFactory.decodeFile(file.path, finalOptions)


        if (bitmap == null || bitmap.isRecycled) {
            throw RuntimeException("Failed to decode bitmap or bitmap is recycled")
        }

        val rotatedBitmap = bitmap.getRotatedBitmap(file)


        if (rotatedBitmap != bitmap) {
            bitmap.recycle()
            bitmap = rotatedBitmap
        }

        var compressQuality = 100
        var outputStream: FileOutputStream? = null

        try {
            do {
                val bmpStream = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, compressQuality, bmpStream)
                val bmpPicByteArray = bmpStream.toByteArray()
                val streamLength = bmpPicByteArray.size

                if (streamLength <= MAXIMAL_SIZE) {

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
        bitmap?.let {
            if (!it.isRecycled) {
                it.recycle()
            }
        }
    }

    return file
}


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


fun Bitmap.getRotatedBitmap(file: File): Bitmap {

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

        this
    }
}

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


        if (rotatedBitmap != source) {

            return rotatedBitmap
        }

        return source
    } catch (e: OutOfMemoryError) {
        throw RuntimeException("Out of memory while rotating bitmap", e)
    }
}