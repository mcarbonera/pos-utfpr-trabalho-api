package com.example.myapitest.ui

import android.graphics.Bitmap
import android.graphics.BitmapShader
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import com.example.myapitest.utils.md5
import com.squareup.picasso.Transformation
import kotlin.random.Random

class CircleTransform : Transformation {
    override fun transform(source: Bitmap): Bitmap {
        val size = source.width.coerceAtMost(source.height)

        val x = (source.width - size) / 2
        val y = (source.height - size) / 2

        val squaredBitmap = Bitmap.createBitmap(source, x, y, size, size)
        if(squaredBitmap != source) {
            source.recycle()
        }

        val bitmap = Bitmap.createBitmap(size, size, source.config)

        val canvas = Canvas(bitmap)
        val paint = Paint()
        val shader = BitmapShader(squaredBitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
        paint.shader = shader
        paint.isAntiAlias = true

        val borderSize = size/5f
        var rect = RectF(0f, 0f, size/1f, size/1f)
        canvas.drawRoundRect(rect, borderSize, borderSize, paint)

        squaredBitmap.recycle()
        return bitmap
    }

    override fun key(): String {
        val seed = Random.nextInt(Int.MAX_VALUE).toString()
        return seed.md5()
    }
}