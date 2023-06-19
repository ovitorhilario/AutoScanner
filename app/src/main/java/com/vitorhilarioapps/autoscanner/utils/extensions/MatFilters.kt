package com.vitorhilarioapps.autoscanner.utils.extensions

import com.vitorhilarioapps.autoscanner.ui.scanner.model.Filters
import org.opencv.core.Mat
import org.opencv.imgproc.Imgproc

fun Mat.getImageFiltered(filter: Filters) : Mat {
    val imgFiltered = this.clone()

    val imgPreview = when (filter) {
        Filters.ORIGINAL -> imgFiltered
        Filters.GRAY -> imgFiltered.gray()
        Filters.THRESHOLD1 -> imgFiltered.threshold()
        Filters.THRESHOLD2 -> imgFiltered.threshold2()
    }

    return imgPreview
}

fun Mat.gray() : Mat {
    val gray = this.clone()
    Imgproc.cvtColor(this, gray, Imgproc.COLOR_BGR2GRAY)
    return gray
}

fun Mat.threshold() : Mat {
    val threshold = this.clone()
    Imgproc.cvtColor(this, threshold, Imgproc.COLOR_BGR2GRAY)
    Imgproc.adaptiveThreshold(threshold, threshold, 255.0, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY, 21, 10.0)
    return threshold
}

fun Mat.threshold2() : Mat {
    val threshold = this.clone()
    Imgproc.cvtColor(this, threshold, Imgproc.COLOR_BGR2GRAY)
    Imgproc.adaptiveThreshold(threshold, threshold, 255.0, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY, 11, 2.0)
    return threshold
}
