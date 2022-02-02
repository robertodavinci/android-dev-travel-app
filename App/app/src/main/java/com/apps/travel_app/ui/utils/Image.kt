package com.apps.travel_app.ui.utils
/**
 * Used for fetching and handling different images when inserting in destinations
 * and trips. Resizes and reshapes images based on their usage in different
 * locations in the app.
 */
import android.app.Activity
import android.content.res.Resources
import android.database.Cursor
import android.graphics.*
import android.graphics.Color.WHITE
import android.net.Uri
import android.provider.MediaStore
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb


fun getRealPathFromURI(contentURI: Uri, activity: Activity): String? {
    val cursor: Cursor? = activity.contentResolver
        .query(contentURI, null, null, null, null)
    return if (cursor == null) {
        contentURI.path
    } else {
        cursor.moveToFirst()
        val idx: Int = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA)
        cursor.getString(idx)
    }
}

fun getCroppedBitmap(
    bitmap: Bitmap,
    width: Int = bitmap.width,
    height: Int = bitmap.height,
    border: Float = 0f
): Bitmap? {
    val usedBitmap = getResizedBitmap(cropToSquare(bitmap), width, height) ?: return null
    val output = Bitmap.createBitmap(
        width,
        height, Bitmap.Config.ARGB_8888
    )

    val canvas = Canvas(output)
    val color = WHITE
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
        paintStroke.color = WHITE
        val offset = border / 2f
        canvas.drawArc(
            RectF(
                offset,
                offset,
                output.width.toFloat() - offset,
                output.height.toFloat() - offset
            ),
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

fun getDominantColor(bitmap: Bitmap?): Int {
    val newBitmap = Bitmap.createScaledBitmap(bitmap!!, 1, 1, true)
    val color = newBitmap.getPixel(0, 0)
    newBitmap.recycle()
    return color
}

fun getTriangularMask(
    url: Int,
    darken: Boolean = false,
    res: Resources
): Bitmap? {
    val bitmap = BitmapFactory.decodeResource(res, url)

    if (bitmap != null) {
        val w = bitmap.width
        val h = bitmap.height
        val output = Bitmap.createBitmap(
            w,
            h + 10, Bitmap.Config.ARGB_8888
        )

        val canvas = Canvas(output)
        val color = WHITE
        val paint = Paint()
        val rect = Rect(0, 0, w, h)
        paint.isAntiAlias = true
        canvas.drawARGB(0, 0, 0, 0)
        paint.color = color
        val path = Path()
        path.reset()
        //path.moveTo(0f, 0f)
        path.moveTo(0f, 0f)
        path.lineTo(0f, 3f * h / 4)
        path.lineTo(w / 2f, h.toFloat())
        path.lineTo(w.toFloat(), 3f * h / 4)
        path.lineTo(w.toFloat(), 0f)


        canvas.drawPath(path, paint)
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
        canvas.drawBitmap(bitmap, rect, rect, paint)
        if (darken) {
            val paintDark = Paint()
            paintDark.isAntiAlias = true
            paintDark.color = Color(0x66111122).toArgb()
            canvas.drawPath(path, paintDark)
        }
        val paintStroke = Paint(Paint.ANTI_ALIAS_FLAG)
        paintStroke.style = Paint.Style.STROKE
        paintStroke.strokeWidth = 15f
        paintStroke.color = WHITE


        return getResizedBitmap(output, 800, 500)
    }
    return null
}
