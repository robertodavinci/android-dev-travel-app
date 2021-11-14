package com.apps.travel_app.ui.utils

import android.graphics.*


fun getCroppedBitmap(bitmap: Bitmap): Bitmap? {
    val output = Bitmap.createBitmap(
        bitmap.width,
        bitmap.height, Bitmap.Config.ARGB_8888
    )
    val canvas = Canvas(output)
    val color = -0xbdbdbe
    val paint = Paint()
    val rect = Rect(0, 0, bitmap.width, bitmap.height)
    paint.isAntiAlias = true
    canvas.drawARGB(0, 0, 0, 0)
    paint.color = color
    canvas.drawCircle(
        bitmap.width / 2f, bitmap.height / 2f,
        bitmap.width / 2f, paint
    )
    paint.setXfermode(PorterDuffXfermode(PorterDuff.Mode.SRC_IN))
    canvas.drawBitmap(bitmap, rect, rect, paint)
    return output
}