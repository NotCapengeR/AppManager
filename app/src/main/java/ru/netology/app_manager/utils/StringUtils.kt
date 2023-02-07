package ru.netology.app_manager.utils

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.util.Base64
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.ByteArrayOutputStream
import java.io.File


object StringUtils {

    /**
     * Used to build output as Hex
     */
    private val DIGITS_LOWER =
        charArrayOf('0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f')

    /**
     * Used to build output as Hex
     */
    private val DIGITS_UPPER =
        charArrayOf('0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F')

    /**
     * Converts an array of bytes into an array of characters representing the hexadecimal values of each byte in order.
     * The returned array will be double the length of the passed array, as it takes two characters to represent any
     * given byte.
     *
     * @param data a byte[] to convert to Hex characters
     * @param toLowerCase `true` converts to lowercase, `false` to uppercase
     * @return A char[] containing hexadecimal characters in the selected case
     */
    fun encodeHex(data: ByteArray, toLowerCase: Boolean): CharArray {
        return encodeHex(data, if (toLowerCase) DIGITS_LOWER else DIGITS_UPPER)
    }

    /**
     * Converts an array of bytes into an array of characters representing the hexadecimal values of each byte in order.
     * The returned array will be double the length of the passed array, as it takes two characters to represent any
     * given byte.
     *
     * @param data a byte[] to convert to Hex characters
     * @param toDigits the output alphabet (must contain at least 16 chars)
     * @return A char[] containing the appropriate characters from the alphabet
     *         For best results, this should be either upper- or lower-case hex.
     */
    fun encodeHex(data: ByteArray, toDigits: CharArray): CharArray {
        val l = data.size
        val out = CharArray(l shl 1)
        // two characters form the hex value.
        var i = 0
        var j = 0
        while (i < l) {
            out[j++] = toDigits[0xF0 and data[i].toInt() ushr 4]
            out[j++] = toDigits[0x0F and data[i].toInt()]
            i++
        }
        return out
    }

    private const val COMPRESSION_LEVEL = 100
    fun encodeImageBase64(drawable: Drawable): String {
        val bitmap = (drawable as BitmapDrawable).bitmap
        val outStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, COMPRESSION_LEVEL, outStream)
        val bitmapContent: ByteArray = outStream.toByteArray()
        val encodedImage: String = Base64.encodeToString(bitmapContent, Base64.NO_WRAP)
        outStream.close()
        return encodedImage
    }

    fun File.asMultipart(field: String, filename: String? = null, contentType: String? = null): MultipartBody.Part {
        return asMultipart(field, filename, contentType?.toMediaType())
    }

    fun File.asMultipart(field: String, filename: String? = null, contentType: MediaType?): MultipartBody.Part {
        return MultipartBody.Part.createFormData(
            field, filename ?: this.name, this.asRequestBody(contentType)
        )
    }

    fun String.asMultipart(): RequestBody = asMultipart("text/plain")

    fun String.asMultipart(contentType: String? = null) = asMultipart(contentType?.toMediaType())
    fun String.asMultipart(contentType: MediaType?) = toRequestBody(contentType)

    fun String.base64encode(): String = Base64.encodeToString(this.toByteArray(), Base64.NO_WRAP)

    fun String.base64decode(): ByteArray = Base64.decode(this, Base64.NO_WRAP)
}

