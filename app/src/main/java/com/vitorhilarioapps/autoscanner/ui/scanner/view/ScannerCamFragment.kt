package com.vitorhilarioapps.autoscanner.ui.scanner.view

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.vitorhilarioapps.autoscanner.R

import com.vitorhilarioapps.autoscanner.app.AppApplication
import com.vitorhilarioapps.autoscanner.app.dataStore
import com.vitorhilarioapps.autoscanner.ui.scanner.ScannerActivity
import com.vitorhilarioapps.autoscanner.ui.scanner.viewmodel.ScannerViewModel
import com.vitorhilarioapps.autoscanner.databinding.FragmentScannerCamBinding
import com.vitorhilarioapps.autoscanner.utils.extensions.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

import org.opencv.android.CameraBridgeViewBase
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2
import org.opencv.core.*
import www.sanju.motiontoast.MotionToast
import www.sanju.motiontoast.MotionToastStyle

class ScannerCamFragment : Fragment(), CvCameraViewListener2 {

    private var _binding : FragmentScannerCamBinding? = null
    private val binding get () = _binding!!
    private val viewModel : ScannerViewModel by activityViewModels()

    private lateinit var mOpenCvCameraView : CameraBridgeViewBase
    private var lastVertices : Array<Point>? = null

    /*-----------------------
    |   LifeCycle Activity  |
    -----------------------*/

    override fun onCreateView(inflater: LayoutInflater, group: ViewGroup?, saved: Bundle?): View {
        _binding = FragmentScannerCamBinding.inflate(LayoutInflater.from(group?.context), group, false)
        setupWindow()
        setupActions()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //
    }

    override fun onResume() {
        super.onResume()

        activateCam()
        setupClickListeners()
    }

    override fun onPause() {
        super.onPause()
        disableCam()
    }

    override fun onDestroy() {
        super.onDestroy()
        disableCam()
    }

    /*---------------
    |   Handle UI   |
    ---------------*/

    private suspend fun getResolutionCode() : Int {
        val settings = requireContext().dataStore.data.first()
        return settings[AppApplication.PREF_RESOLUTION] ?: 0
    }

    private fun setupActions() {
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                requireActivity().finish()
            }
        }

        requireActivity().onBackPressedDispatcher.addCallback(requireActivity(), callback)
    }

    private fun setupWindow() {
        requireActivity().let {
            it.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            it.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    private fun refreshCurrentFragment(){
        val navController = findNavController()
        val id = navController.currentDestination?.id
        navController.popBackStack(id!!,true)
        navController.navigate(id)
    }

    private fun setupClickListeners() {

        binding.btnCapture.setOnClickListener {
            if (viewModel.currentImg.value != null) {
                val action = ScannerCamFragmentDirections.actionCamToPreview()
                findNavController().navigate(action)
            }
        }

        binding.btnCamReload.setOnClickListener {
            refreshCurrentFragment()
        }
    }

    private fun activateCam() {
        val resolutionCode = runBlocking { getResolutionCode() }

        mOpenCvCameraView = binding.javaCameraViewOpencv
        mOpenCvCameraView.apply {
            setCameraPermissionGranted()
            setCameraIndex(CameraBridgeViewBase.CAMERA_ID_ANY)
            visibility = SurfaceView.VISIBLE

            if (resolutionCode == 1) {
                setMaxFrameSize(640, 480)
            } else if (resolutionCode == 2) {
                setMaxFrameSize(1280, 720)
            }

            setCvCameraViewListener(this@ScannerCamFragment)
            enableView()
        }
    }

    private fun disableCam() {
        if (this::mOpenCvCameraView.isInitialized) {
            mOpenCvCameraView.disableView()
            mOpenCvCameraView.visibility = SurfaceView.GONE
        }
    }

    /*-------------------
    |   OpenCV Methods  |
    -------------------*/

    private fun setCurrentImage(img: Mat) {
        requireActivity().runOnUiThread {
            val image = img.clone()
            Core.rotate(image, image, Core.ROTATE_90_CLOCKWISE)

            if (image.isValid()) {
                viewModel.currentImg.value = image
            }
        }
    }

    /* --------------------
    |   OpenCV CallBacks  |
    ---------------------*/

    override fun onCameraViewStarted(width: Int, height: Int) {
        Log.i(ScannerActivity.TAG, "Camera View Started")
    }

    override fun onCameraViewStopped() {
        Log.i(ScannerActivity.TAG, "Camera View Stopped")
    }

    override fun onCameraFrame(inputFrame: CameraBridgeViewBase.CvCameraViewFrame?) : Mat {
        val imgRgba = inputFrame?.rgba() ?: Mat()
        val imgGray = inputFrame?.gray() ?: Mat()

        try {
            if (binding.switchCamAutoDetect.isChecked) {
                val imgEdges = imgGray.getEdges()
                val (contours, hierarchy) = imgEdges.findContours()

                if (contours.isEmpty() || contours.size == 0) {
                    setCurrentImage(imgRgba)
                    return imgRgba
                }

                val (maxPoints, maxContour) = contours.getQuadrilateral(imgRgba.size())

                if (maxPoints != null) {
                    lastVertices = maxPoints
                    val croppedImg = imgRgba.getCroppedImg(maxPoints)
                    imgRgba.drawContour(maxPoints)

                    setCurrentImage(croppedImg)
                } else {
                    setCurrentImage(imgRgba)
                }
            } else {
                setCurrentImage(imgRgba)
            }

        } catch (err: Exception) {
            MotionToast.darkToast(requireActivity(),
                resources.getString(R.string.error) + " ☹️",
                err.message.toString(),
                MotionToastStyle.ERROR,
                MotionToast.GRAVITY_BOTTOM,
                MotionToast.LONG_DURATION,
                ResourcesCompat.getFont(requireActivity(), R.font.dmsans_regular))

            Log.e("CAMERA", err.message.toString())
            setCurrentImage(imgRgba)
        }

        return imgRgba
    }
}