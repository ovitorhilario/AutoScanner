package com.vitorhilarioapps.autoscanner.utils.extensions

import android.annotation.SuppressLint
import java.text.SimpleDateFormat
import java.util.*

@SuppressLint("SimpleDateFormat")
fun getCurrentTime() : String {
    val time = Calendar.getInstance().time
    val formatter = SimpleDateFormat("yyyy-MM-dd-HH:mm")
    val fileName = buildString {
        append("AutoScanner-")
        append(formatter.format(time))
    }

    return fileName
}