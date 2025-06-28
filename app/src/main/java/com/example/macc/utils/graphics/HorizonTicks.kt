package com.example.macc.utils.graphics

import android.graphics.Bitmap
import org.opencv.android.Utils
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.core.Point
import org.opencv.core.Scalar
import org.opencv.imgproc.Imgproc

object HorizonTicks {
    fun create(heading: Int, width: Int, height: Int = 100): Bitmap {
        val mat = Mat.zeros(height, width, CvType.CV_8UC4)
        val centerY = height / 2.0
        val color = Scalar(255.0, 255.0, 255.0, 255.0)
        // horizon line
        Imgproc.line(mat, Point(0.0, centerY), Point(width.toDouble(), centerY), color, 2)

        val tickCount = 60
        val spacing = width.toDouble() / tickCount
        val start = (heading - tickCount / 2 + 360) % 360

        for (i in 0..tickCount) {
            val x = i * spacing
            val deg = (start + i) % 360
            val tickHeight = if (deg % 10 == 0) height * 0.6 else height * 0.3
            val p1 = Point(x, centerY - tickHeight / 2)
            val p2 = Point(x, centerY + tickHeight / 2)
            Imgproc.line(mat, p1, p2, color, 2)
        }

        val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        Utils.matToBitmap(mat, bmp)
        mat.release()
        return bmp
    }
}

