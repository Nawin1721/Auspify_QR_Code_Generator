package com.naveen.qrcodegenerator



import android.annotation.SuppressLint
import android.graphics.Bitmap
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import androidx.core.graphics.createBitmap
import androidx.core.graphics.set

object QRGenerator {

    @SuppressLint("UseKtx")
    fun generateQRCode(
        text: String,
        size: Int = 512
    ): Bitmap? {

        if (text.isBlank()) return null

        return try {
            val hints = mapOf(
                EncodeHintType.MARGIN to 1,
                EncodeHintType.ERROR_CORRECTION to
                        ErrorCorrectionLevel.H
            )

            val bitMatrix = MultiFormatWriter().encode(
                text,
                BarcodeFormat.QR_CODE,
                size,
                size,
                hints
            )

            val bitmap = createBitmap(size, size)

            for (x in 0 until size) {
                for (y in 0 until size) {
                    bitmap[x, y] = if (bitMatrix[x, y])
                        android.graphics.Color.BLACK
                    else
                        android.graphics.Color.WHITE
                }
            }

            bitmap

        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
