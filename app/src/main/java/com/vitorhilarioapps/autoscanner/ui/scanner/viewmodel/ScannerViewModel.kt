package com.vitorhilarioapps.autoscanner.ui.scanner.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import org.opencv.core.Mat

class ScannerViewModel : ViewModel() {

    val currentImg = MutableLiveData<Mat>()
}