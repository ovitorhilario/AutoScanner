package com.vitorhilarioapps.autoscanner.utils.extensions

import android.graphics.Bitmap
import org.opencv.android.Utils
import org.opencv.core.*
import org.opencv.imgproc.Imgproc
import kotlin.collections.ArrayList
import kotlin.math.*

fun Mat.toBitmap(config: Bitmap.Config = Bitmap.Config.ARGB_8888): Bitmap {
    val bitmap = Bitmap.createBitmap(this.cols(), this.rows(), config)
    Utils.matToBitmap(this, bitmap)
    return bitmap
}

fun Mat.isValid() : Boolean = width() > 0 && height() > 0

fun Mat.findContours() : Pair<ArrayList<MatOfPoint>, Mat> {
    val imgSource = this
    val contours = ArrayList<MatOfPoint>()
    val hierarchy = Mat()
    Imgproc.findContours(imgSource, contours, hierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_NONE)
    return contours to hierarchy
}

fun Mat.drawContour(vertices: Array<Point>) {
    val tl: Point = vertices[0]
    val tr: Point = vertices[1]
    val br: Point = vertices[2]
    val bl: Point = vertices[3]
    val color = Scalar(0.0, 244.0, 62.0)

    Imgproc.drawContours(this, listOf(MatOfPoint(*vertices)), -1, color, 3)

    Imgproc.circle(this, tl, 7, color, -1)
    Imgproc.circle(this, tr, 7, color, -1)
    Imgproc.circle(this, br, 7, color, -1)
    Imgproc.circle(this, bl, 7, color, -1)
}

fun Mat.getCroppedImg(vertices: Array<Point>) : Mat {
    val src = this

    val tl: Point = vertices[0]
    val tr: Point = vertices[1]
    val br: Point = vertices[2]
    val bl: Point = vertices[3]

    val widthA = sqrt((br.x - bl.x).pow(2.0) + (br.y - bl.y).pow(2.0))
    val widthB = sqrt((tr.x - tl.x).pow(2.0) + (tr.y - tl.y).pow(2.0))
    val maxWidth = max(widthA.toInt(), widthB.toInt())

    val heightA = sqrt((tr.x - br.x).pow(2.0) + (tr.y - br.y).pow(2.0))
    val heightB = sqrt((tl.x - bl.x).pow(2.0) + (tl.y - bl.y).pow(2.0))
    val maxHeight = max(heightA.toInt(), heightB.toInt())

    val sourceMat = MatOfPoint2f(tl, tr, br, bl)

    val destinyMat = MatOfPoint2f(
        Point(0.0, 0.0),
        Point(maxWidth - 1.0, 0.0),
        Point(maxWidth - 1.0, maxHeight - 1.0),
        Point(0.0, maxHeight - 1.0)
    )

    val imgDoc = Mat(maxHeight, maxWidth, CvType.CV_8UC4)

    val m = Imgproc.getPerspectiveTransform(sourceMat, destinyMat)
    Imgproc.warpPerspective(src, imgDoc, m, imgDoc.size())

    return imgDoc
}

fun Mat.getEdges() : Mat {

    val imgSource = this

    // Morph process - Close
    val kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, Size(5.0, 5.0))
    Imgproc.morphologyEx(imgSource, imgSource, Imgproc.MORPH_CLOSE, kernel, Point(-1.0, -1.0), 3)

    // Blur
    Imgproc.GaussianBlur(imgSource, imgSource, Size(5.0, 5.0), 0.0)

    // Detect edges
    val imgEdges = Mat()
    Imgproc.Canny(imgSource, imgEdges, 10.0, 70.0)

    // Morph process - Dilate
    val kernelForDilate = Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, Size(5.0, 5.0))
    Imgproc.dilate(imgEdges, imgEdges, kernelForDilate)

    return imgEdges
}

fun ArrayList<MatOfPoint>.getQuadrilateral(ImgSrcSize: Size) : Pair<Array<Point>?, MatOfPoint?> {
    val contours = this
    var maxQuadrilateral : MatOfPoint? = null
    var maxPoints : Array<Point>? = null
    var maxArea = 0.0

    //val imgSourceArea = ImgSrcSize.width * ImgSrcSize.height
    //val minQAreaRatio = 0.02

    for (contour in contours) {
        val approxCurve = MatOfPoint2f(*contour.toArray())
        val peri = Imgproc.arcLength(approxCurve, true)
        Imgproc.approxPolyDP(approxCurve, approxCurve, 0.02 * peri, true)
        val points = approxCurve.toArray()

        if (points.size == 4) {
            val foundPoints = points.sortPoints()

            val qContour = MatOfPoint(*foundPoints)
            val qArea = Imgproc.contourArea(qContour)

            //val qAreaRatio = qArea / imgSourceArea

            if (qArea > maxArea && foundPoints.isRectangle()) {
                maxArea = qArea
                maxPoints = foundPoints
                maxQuadrilateral = qContour
            }
        }
    }

    return maxPoints to maxQuadrilateral
}

private fun Array<Point>.isRectangle(): Boolean {
    val angles = mutableListOf<Double>()

    for (i in 0..3) {
        val p1 = this[i]
        val p2 = this[(i + 1) % 4]
        val p3 = this[(i + 2) % 4]

        val v1 = Point(p2.x - p1.x, p2.y - p1.y)
        val v2 = Point(p3.x - p2.x, p3.y - p2.y)

        val dotProduct = v1.x * v2.x + v1.y * v2.y
        val crossProduct = v1.x * v2.y - v1.y * v2.x

        val angle = atan2(crossProduct, dotProduct) * 180 / Math.PI
        angles.add(abs(angle))
    }

    val meanAngle = angles.average()
    val stdDev = sqrt(angles.map { (it - meanAngle).pow(2.0) }.average())

    return abs(meanAngle - 90.0) < 10.0 && stdDev < 10.0
}

private fun Array<Point>.sortPoints(): Array<Point> {
    return arrayOf(
        this.minBy { it.x + it.y },  // top-left corner - tl
        this.minBy { it.y - it.x },  // top-right corner - tr
        this.maxBy { it.x + it.y },  // bottom-right corner - br
        this.maxBy { it.y - it.x }   // bottom-left corner - bl
    )
}