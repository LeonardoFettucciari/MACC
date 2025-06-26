package com.example.macc2025.utils.graphics

import android.graphics.Bitmap
import org.opencv.android.Utils
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.core.Point
import org.opencv.core.Scalar
import org.opencv.imgproc.Imgproc

object CircumflexIcon {
    fun create(size: Int = 200): Bitmap {
        val mat = Mat.zeros(size, size, CvType.CV_8UC4)
        val center = size / 2.0
        val color = Scalar(255.0, 0.0, 0.0, 255.0)
        val thickness = (size * 0.05).toInt()
        val offset = size * 0.1
        val p1 = Point(center - offset, center + offset)
        val p2 = Point(center, center - offset)
        val p3 = Point(center + offset, center + offset)
        Imgproc.line(mat, p1, p2, color, thickness)
        Imgproc.line(mat, p2, p3, color, thickness)
        val bmp = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        Utils.matToBitmap(mat, bmp)
        mat.release()
        return bmp
    }
}
