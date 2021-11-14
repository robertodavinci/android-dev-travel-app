package com.apps.travel_app.ui.utils

import android.graphics.*


fun getCroppedBitmap(
    bitmap: Bitmap,
    width: Int = bitmap.width,
    height: Int = bitmap.height,
    border: Float = 0f
): Bitmap? {
    var usedBitmap = getResizedBitmap(cropToSquare(bitmap), width, height) ?: return null
    val output = Bitmap.createBitmap(
        width,
        height, Bitmap.Config.ARGB_8888
    )

    val canvas = Canvas(output)
    val color = Color.WHITE
    val paint = Paint()
    val rect = Rect(0, 0, usedBitmap.width, usedBitmap.height)
    paint.isAntiAlias = true
    canvas.drawARGB(0, 0, 0, 0)
    paint.color = color
    canvas.drawCircle(
        width / 2f, height / 2f,
        width / 2f, paint
    )
    paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
    canvas.drawBitmap(usedBitmap, rect, rect, paint)
    if (border > 0) {
        val paintStroke = Paint(Paint.ANTI_ALIAS_FLAG)
        paintStroke.style = Paint.Style.STROKE
        paintStroke.strokeWidth = border
        paintStroke.color = Color.WHITE
        val offset = border / 2f
        canvas.drawArc(
            RectF(offset, offset, output.width.toFloat() - offset, output.height.toFloat() - offset),
            0f,
            360f,
            true,
            paintStroke
        )
    }
    return output
}

fun cropToSquare(bitmap: Bitmap): Bitmap {
    val width = bitmap.width
    val height = bitmap.height
    val newWidth = if (height > width) width else height
    val newHeight = if (height > width) height - (height - width) else height
    var cropW = (width - height) / 2
    cropW = if (cropW < 0) 0 else cropW
    var cropH = (height - width) / 2
    cropH = if (cropH < 0) 0 else cropH
    return Bitmap.createBitmap(bitmap, cropW, cropH, newWidth, newHeight)
}

fun getResizedBitmap(bm: Bitmap, newWidth: Int, newHeight: Int): Bitmap? {
    val width = bm.width
    val height = bm.height
    val scaleWidth = newWidth.toFloat() / width
    val scaleHeight = newHeight.toFloat() / height
    val matrix = Matrix()
    matrix.postScale(scaleWidth, scaleHeight)

    val resizedBitmap = Bitmap.createBitmap(
        bm, 0, 0, width, height, matrix, false
    )
    bm.recycle()
    return resizedBitmap
}