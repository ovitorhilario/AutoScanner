package com.vitorhilarioapps.autoscanner.ui.scanner

import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.navigation.fragment.NavHostFragment
import com.vitorhilarioapps.autoscanner.R
import com.vitorhilarioapps.autoscanner.databinding.ActivityScannerBinding
import com.vitorhilarioapps.autoscanner.ui.scanner.viewmodel.ScannerViewModel
import com.vitorhilarioapps.autoscanner.utils.extensions.isValid
import org.opencv.android.Utils
import org.opencv.core.Mat

class ScannerActivity : AppCompatActivity() {

    private val binding by lazy { ActivityScannerBinding.inflate(layoutInflater) }
    private val viewModel : ScannerViewModel by viewModels()

    /*-----------------------
    |   LifeCycle Activity  |
    -----------------------*/

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val messageReceived = intent.getStringExtra(MESSAGE_TO_SCANNER)
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        if (messageReceived != null) {
            val response = trySetImage(messageReceived)

            if (response) {
                navController.setGraph(R.navigation.nav_graph_scanner, bundleOf(OPEN_PREVIEW to true))
            }
        } else {
            navController.setGraph(R.navigation.nav_graph_scanner, bundleOf(OPEN_PREVIEW to false))
        }
    }

    private fun trySetImage(uri: String) : Boolean {
        val imageUri = Uri.parse(uri)
        val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, imageUri)

        val newMat = Mat()
        Utils.bitmapToMat(bitmap, newMat)

        return if (newMat.isValid()) {
            viewModel.currentImg.value = newMat
            true
        } else false
    }

    companion object {
        const val TAG = "ScannerActivity"
        const val MESSAGE_TO_SCANNER = "MESSAGE_TO_SCANNER"
        const val OPEN_PREVIEW = "OPEN_PREVIEW"
    }
}
