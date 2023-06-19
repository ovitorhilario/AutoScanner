package com.vitorhilarioapps.autoscanner.ui.home

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.ads.*
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.vitorhilarioapps.autoscanner.R
import com.vitorhilarioapps.autoscanner.app.AppApplication
import com.vitorhilarioapps.autoscanner.app.dataStore
import com.vitorhilarioapps.autoscanner.databinding.ActivityHomeBinding
import com.vitorhilarioapps.autoscanner.ui.home.adapter.HomeAdapter
import com.vitorhilarioapps.autoscanner.ui.home.adapter.HomeItem
import com.vitorhilarioapps.autoscanner.ui.scanner.ScannerActivity
import com.vitorhilarioapps.autoscanner.ui.scanner.model.Filters
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import www.sanju.motiontoast.MotionToast
import www.sanju.motiontoast.MotionToastStyle

class HomeActivity : AppCompatActivity() {

    private val binding by lazy { ActivityHomeBinding.inflate(layoutInflater) }
    private var mInterstitialAd: InterstitialAd? = null
    private var adIsLoading: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        MobileAds.initialize(this)
        setupUI()
    }

    private fun loadAd() {
        var adRequest = AdRequest.Builder().build()

        InterstitialAd.load(this, resources.getString(R.string.unit_id_test_interstitial), adRequest, object : InterstitialAdLoadCallback() {
            override fun onAdFailedToLoad(adError: LoadAdError) {
                mInterstitialAd = null
                adIsLoading = false
            }

            override fun onAdLoaded(interstitialAd: InterstitialAd) {
                mInterstitialAd = interstitialAd
                adIsLoading = false
            }
        })
    }

    private fun setupUI() {

        if (!adIsLoading && mInterstitialAd == null) {
            adIsLoading = true
            loadAd()
        }

        setupAdapter()
        setupTopAppBar()

        binding.fabOpenScanner.setOnClickListener {
            if (hasPermissions()) {
                if (mInterstitialAd != null) {
                    showInterstitialAd()
                } else {
                    startCameraActivity()
                }
            } else {
                requestCameraPermission()
            }
        }
    }

    private fun showInterstitialAd() {
        mInterstitialAd?.fullScreenContentCallback =
            object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    mInterstitialAd = null
                    startCameraActivity()
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    mInterstitialAd = null
                    loadAd()
                }

                override fun onAdShowedFullScreenContent() {
                    Log.d(TAG, "Ad showed fullscreen content.")
                }
            }

        mInterstitialAd?.show(this)
    }

    private fun setupTopAppBar() {
        binding.topAppBarHome.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.menu_cam_size -> {
                    openDialogCamSize()
                    true
                }
                R.id.menu_bug_report -> {
                    openDialogBugReport()
                    true
                }
                else -> false
            }
        }
    }

    private fun openDialogBugReport() {
        MaterialAlertDialogBuilder(this)
            .setTitle(resources.getString(R.string.bug_report))
            .setMessage(resources.getString(R.string.bug_report_question))
            .setNeutralButton(resources.getString(R.string.cancel)) { _, _ -> }
            .setPositiveButton(resources.getString(R.string.report)) { _, _ ->
                val url = resources.getString(R.string.bug_report_url)
                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                startActivity(browserIntent)
            }
            .show()
    }

    private fun openDialogCamSize() {
        val singleItems = arrayOf("Auto - Original", "640x480", "1280x720")
        val currentCheckedItem = runBlocking { getResolutionCode() }
        var finalCheckedItem = currentCheckedItem

        MaterialAlertDialogBuilder(this)
            .setTitle(resources.getString(R.string.resolution))
            .setNeutralButton(resources.getString(R.string.cancel)) { _, _ -> }
            .setPositiveButton(resources.getString(R.string.ok)) { _, _ ->
                if (finalCheckedItem != currentCheckedItem) {
                    lifecycleScope.launch { saveResolutionCode(finalCheckedItem) }
                }
            }
            .setSingleChoiceItems(singleItems, currentCheckedItem) { dialog, which ->
                finalCheckedItem = which
            }
            .show()
    }

    private fun setupAdapter() {
        binding.rvHome.adapter = HomeAdapter(
            listData = listOf(
                HomeItem.FilterList(listOf(
                    Filters.ORIGINAL,
                    Filters.GRAY ,
                    Filters.THRESHOLD1,
                    Filters.THRESHOLD2
                )),
                HomeItem.ImagePicker(resources.getString(R.string.pick_image_icon)),
                HomeItem.Card(
                    resources.getString(R.string.features_tittle),
                    resources.getString(R.string.features_topic_1),
                    resources.getString(R.string.features_topic_2),
                    resources.getString(R.string.features_topic_3),
                    resources.getString(R.string.features_icon)),
                HomeItem.Card(
                    resources.getString(R.string.tips_tittle),
                    resources.getString(R.string.tips_topic_1),
                    resources.getString(R.string.tips_topic_2),
                    resources.getString(R.string.tips_topic_3),
                    resources.getString(R.string.tips_icon)),
                HomeItem.Author("")
            ),

            actionOpenImagePicker = { pickImage() }
        )
    }

    private val getContent = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { imageUri -> startCameraActivity(imageUri.toString()) }
    }

    private fun pickImage() = getContent.launch("image/*")

    private fun startCameraActivity(uri: String? = null) {
        val intent = Intent(this, ScannerActivity::class.java)
        uri?.let { intent.putExtra(MESSAGE_TO_SCANNER, it) }
        startActivity(intent)
    }

    private fun requestCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), CAMERA_PERMISSION_CODE)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                MotionToast.darkToast(this,
                    resources.getString(R.string.success) + " \uD83D\uDE00",
                    resources.getString(R.string.permission_granted),
                    MotionToastStyle.SUCCESS,
                    MotionToast.GRAVITY_BOTTOM,
                    MotionToast.SHORT_DURATION,
                    ResourcesCompat.getFont(this, R.font.dmsans_regular))

            } else {
                MotionToast.darkToast(this,
                    resources.getString(R.string.error) + " ☹️",
                    resources.getString(R.string.permission_granted),
                    MotionToastStyle.ERROR,
                    MotionToast.GRAVITY_BOTTOM,
                    MotionToast.SHORT_DURATION,
                    ResourcesCompat.getFont(this, R.font.dmsans_regular))

            }
        }
    }

    private fun hasPermissions() : Boolean {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
    }

    private suspend fun getResolutionCode() : Int {
        val settings = dataStore.data.first()
        return settings[AppApplication.PREF_RESOLUTION] ?: 0
    }

    private suspend fun saveResolutionCode(code: Int) {
        dataStore.edit { settings ->
            settings[AppApplication.PREF_RESOLUTION] = code
        }

        val resolutionType = when (code) {
            0 -> "Auto"
            1 -> "640x480"
            2 -> "1280x720"
            else -> "Auto"
        }

        MotionToast.darkToast(this,
            resources.getString(R.string.success) + " \uD83D\uDE00",
            buildString {
                append(resources.getString(R.string.saved_msg) + " ")
                append(resolutionType)
            },
            MotionToastStyle.SUCCESS,
            MotionToast.GRAVITY_BOTTOM,
            MotionToast.SHORT_DURATION,
            ResourcesCompat.getFont(this, R.font.dmsans_regular))
    }

    companion object {
        const val TAG = "HomeActivity"
        private const val CAMERA_PERMISSION_CODE = 100
        const val MESSAGE_TO_SCANNER = "MESSAGE_TO_SCANNER"
        const val assets_path = "file:///android_asset/"
    }
}
