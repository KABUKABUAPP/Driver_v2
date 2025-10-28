package com.kabukabu.driver.components.utils_functions

import android.content.Context
import android.net.Uri
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.text.NumberFormat
import java.util.Currency
import java.util.Locale
import kotlin.collections.forEach


fun convertUrisToFiles(context: Context, uris: List<Uri>): List<File> {
    val files = mutableListOf<File>()

    uris.forEach { uri ->
        val inputStream = context.contentResolver.openInputStream(uri)
        val tempFile = File.createTempFile("temp_image_", ".tmp", context.cacheDir)

        inputStream?.use { input ->
            tempFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }

        files.add(tempFile)
    }

    return files
}

fun convertUriToFile(context: Context, uri: Uri?): File? {
    if (uri == null) return null

    return try {
        val inputStream = context.contentResolver.openInputStream(uri) ?: return null
        val tempFile = File.createTempFile("temp_image_", ".tmp", context.cacheDir)

        inputStream.use { input ->
            tempFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }

        tempFile
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

fun File?.toMultipartPart(fieldName: String): MultipartBody.Part? {
    return this?.let {
        MultipartBody.Part.createFormData(
            name = fieldName,
            filename = it.name,
            body = it.asRequestBody("image/*".toMediaTypeOrNull())
        )
    }
}


