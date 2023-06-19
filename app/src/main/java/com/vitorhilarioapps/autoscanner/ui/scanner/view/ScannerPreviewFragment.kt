package com.vitorhilarioapps.autoscanner.ui.scanner.view

import android.Manifest
import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.MutableLiveData
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.vitorhilarioapps.autoscanner.R

import com.vitorhilarioapps.autoscanner.ui.scanner.adapter.ScannerPreviewAdapter
import com.vitorhilarioapps.autoscanner.ui.scanner.model.Filters
import com.vitorhilarioapps.autoscanner.ui.scanner.viewmodel.ScannerViewModel
import com.vitorhilarioapps.autoscanner.databinding.FragmentScannerPreviewBinding
import com.vitorhilarioapps.autoscanner.utils.extensions.getCurrentTime
import com.vitorhilarioapps.autoscanner.utils.extensions.getImageFiltered
import com.vitorhilarioapps.autoscanner.utils.extensions.toBitmap
import org.opencv.core.Mat
import www.sanju.motiontoast.MotionToast
import www.sanju.motiontoast.MotionToastStyle
import java.io.OutputStream

class ScannerPreviewFragment : Fragment() {

    private var _binding : FragmentScannerPreviewBinding? = null
    private val binding get () = _binding!!
    private val viewModel : ScannerViewModel by activityViewModels()
    private val currentFilter =  MutableLiveData<Filters>()
    private val args by navArgs<ScannerPreviewFragmentArgs>()
    private lateinit var adView : AdView

    override fun onCreateView(inflater: LayoutInflater, group: ViewGroup?, saved: Bundle?): View {
        _binding = FragmentScannerPreviewBinding.inflate(LayoutInflater.from(group?.context), group, false)
        setupWindow()
        setupActions()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupAds()
        setupFilterObserver()
        setupUI()
        setupClickListeners()
    }

    override fun onResume() {
        super.onResume()
        if (this::adView.isInitialized) {
            adView.resume()
        }
    }

    override fun onPause() {
        if (this::adView.isInitialized) {
            adView.pause()
        }
        super.onPause()
    }

    override fun onDestroy() {
        if (this::adView.isInitialized) {
            adView.destroy()
        }
        super.onDestroy()
    }

    /*------------------
    |   Handle AdMob   |
    ------------------*/

    private fun setupAds() {
        MobileAds.initialize(requireActivity())

        adView = binding.adsContainer
        val adRequest = AdRequest.Builder().build()

        adView.loadAd(adRequest)
        adView.adListener = adListenerCallback
    }

    private val adListenerCallback = object : AdListener() {
        override fun onAdClicked() {
            // Code to be executed when the user clicks on an ad.
            Log.i(TAG, "Ad Clicked")
        }

        override fun onAdClosed() {
            // Code to be executed when the user is about to return
            // to the app after tapping on an ad.
            Log.i(TAG, "Ad Closed")
        }

        override fun onAdFailedToLoad(adError : LoadAdError) {
            // Code to be executed when an ad request fails.
            Log.i(TAG, "Ad Failed")
        }

        override fun onAdImpression() {
            // Code to be executed when an impression is recorded
            // for an ad.
            Log.i(TAG, "Ad Impression")
        }

        override fun onAdLoaded() {
            // Code to be executed when an ad finishes loading.
            Log.i(TAG, "Ad Loaded")
        }

        override fun onAdOpened() {
            // Code to be executed when an ad opens an overlay that
            // covers the screen.
            Log.i(TAG, "Ad Opened")
        }
    }

    /*---------------
    |   Handle UI   |
    ---------------*/

    private fun setupUI() {
        setupAdapter()
    }

    private fun setupAdapter() {
        binding.rvPreviewFilterList.adapter = ScannerPreviewAdapter(
            filterList = listOf(Filters.ORIGINAL, Filters.GRAY, Filters.THRESHOLD1, Filters.THRESHOLD2),
            actionApplyFilter = { filter ->
                when(filter) {
                    Filters.ORIGINAL -> currentFilter.value = Filters.ORIGINAL
                    Filters.GRAY -> currentFilter.value = Filters.GRAY
                    Filters.THRESHOLD1 -> currentFilter.value = Filters.THRESHOLD1
                    Filters.THRESHOLD2 -> currentFilter.value = Filters.THRESHOLD2
                }
            }
        )
    }

    private fun setupFilterObserver() {
        currentFilter.observe(viewLifecycleOwner) { filter ->
            viewModel.currentImg.value?.let { img ->
                setImagePreview(img.getImageFiltered(filter))
            }
        }
    }

    private fun setupClickListeners() {

        binding.toolbarImgEditor.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.menu_save_image -> {
                    if (hasPermissions()) {
                        val currentImg = viewModel.currentImg.value
                        val currentFilter = currentFilter.value

                        if (currentFilter != null && currentImg != null) {
                            saveImage(this@ScannerPreviewFragment.requireContext(), currentImg.getImageFiltered(currentFilter).toBitmap())
                        }

                    } else {
                        requestExternalStoragePermissions()
                    }

                    true
                }
                else -> false
            }
        }

        binding.toolbarImgEditor.setNavigationOnClickListener {
            val navController = findNavController()
            navController.popBackStack()

            if (args.pickedImage) {
                requireActivity().finish()
            } else {
                ScannerPreviewFragmentDirections.actionPreviewToCam()
            }
        }
    }

    private fun setupWindow() {
        requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
    }

    private fun setupActions() {
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val navController = findNavController()
                navController.popBackStack()

                if (args.pickedImage) {
                    requireActivity().finish()
                } else {
                    ScannerPreviewFragmentDirections.actionPreviewToCam()
                }
            }
        }

        requireActivity().onBackPressedDispatcher.addCallback(requireActivity(), callback)

        currentFilter.value = Filters.ORIGINAL
    }

    /*--------------------------
    |   Handle Image Filters   |
    --------------------------*/

    private fun setImagePreview(img: Mat) {
        val bitMap = img.toBitmap()
        binding.ivPreview.setImageBitmap(bitMap)
    }

    private fun saveImage(context: Context, bitmap: Bitmap): Boolean {
        val outputStream: OutputStream?
        val contentResolver: ContentResolver = context.contentResolver
        val fileName = getCurrentTime()

        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/png")

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
                put(MediaStore.MediaColumns.IS_PENDING, 1)
            }
        }

        val imageUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

        try {
            outputStream = imageUri?.let { contentResolver.openOutputStream(it) }

            if (outputStream != null) {
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                outputStream.close()
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                contentValues.clear()
                contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
                contentResolver.update(imageUri!!, contentValues, null, null)
            }

            showSavedAs(fileName)

            return true
        } catch (e: Exception) {
            imageUri?.let { contentResolver.delete(it, null, null) }
            e.printStackTrace()
            showErrorSave()
            return false
        }
    }

    private fun showErrorSave() {
        MotionToast.darkToast(requireActivity(),
            resources.getString(R.string.error)+ " ☹️",
            resources.getString(R.string.unsaved_msg),
            MotionToastStyle.ERROR,
            MotionToast.GRAVITY_BOTTOM,
            MotionToast.SHORT_DURATION,
            ResourcesCompat.getFont(requireActivity(), R.font.dmsans_regular))
    }

    private fun showSavedAs(message: String) {
        MotionToast.darkToast(requireActivity(),
            resources.getString(R.string.success) + " \uD83D\uDE00",
            buildString {
                append(resources.getString(R.string.saved_msg) + " ")
                append(message)
            },
            MotionToastStyle.SUCCESS,
            MotionToast.GRAVITY_BOTTOM,
            MotionToast.SHORT_DURATION,
            ResourcesCompat.getFont(requireActivity(), R.font.dmsans_regular))
    }

    /*-----------------
    |   Permissions   |
    -----------------*/

    private fun requestExternalStoragePermissions() {
        ActivityCompat.requestPermissions(requireActivity(), PERMISSIONS, EXTERNAL_STORAGE_CODE)
    }

    private fun hasPermissions() : Boolean {
        var allPermissionsGranted = true

        for (permission in PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(requireActivity(), permission) != PackageManager.PERMISSION_GRANTED) {
                allPermissionsGranted = false
                break
            }
        }

        return allPermissionsGranted
    }

    companion object {
        private val PERMISSIONS = arrayOf(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
        const val TAG = "ScannerPreviewFragment"
        const val assets_path = "file:///android_asset/"
        const val EXTERNAL_STORAGE_CODE = 101
    }
}