package com.monsters.mobimon.feature.auth

import android.graphics.Bitmap
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.Painter
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
internal fun githubQrCode(uri: String?): Painter? {
    val painter by produceState<Painter?>(null, uri) {
        value =
            if (uri == null) {
                null
            } else {
                withContext(Dispatchers.Default) {
                    BitmapPainter(createGitHubQrCode(uri).asImageBitmap())
                }
            }
    }
    return if (uri == null) null else painter
}

internal fun createGitHubQrCode(uri: String): Bitmap {
    require(uri == "https://github.com/login/device")
    val size = 512
    val matrix = QRCodeWriter().encode(uri, BarcodeFormat.QR_CODE, size, size, mapOf(EncodeHintType.MARGIN to 4))
    val pixels = IntArray(size * size) { index -> if (matrix[index % size, index / size]) 0xff000000.toInt() else -1 }
    return Bitmap.createBitmap(pixels, size, size, Bitmap.Config.ARGB_8888)
}
