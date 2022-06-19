package com.MelquiApps.vibration_test.pages

import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.MelquiApps.vibration_test.R
import com.google.android.ads.nativetemplates.NativeTemplateStyle
import com.google.android.ads.nativetemplates.TemplateView
import com.google.android.gms.ads.*
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import kotlinx.android.synthetic.main.activity_vibration_tester.*
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


@DelicateCoroutinesApi
class VibrationTesterActivity : AppCompatActivity() {

    private lateinit var vib: Vibrator
    private var isNotVibrating = true
    private var pattern = longArrayOf(0, 10000, 200)

    //region Ads
    private var initializingApp = true
    private lateinit var topNativeBannerAdLoader: AdLoader
    private lateinit var bottomNativeBannerAdLoader: AdLoader
    private var listOfNativeAdsToDestroy: MutableList<NativeAd> = mutableListOf()
    private var loadAds: Job? = null
    private var isFirstClick: Boolean = true
    private var showTopAd: Job? = null
    private var showBottomAd: Job? = null

    private val topNativeAdUnitID: String = "ca-app-pub-2019856840997362/9485061409"
    private val bottomNativeAdUnitID: String = "ca-app-pub-2019856840997362/2347973263"
    //private val topNativeAdUnitID: String = "ca-app-pub-3940256099942544/2247696110"
    //private val bottomNativeAdUnitID: String = "ca-app-pub-3940256099942544/1044960115"

    private fun loadAds() {
        if (loadAds != null) loadAds!!.cancel()
        loadAds = lifecycleScope.launch {
            if (initializingApp) {
                MobileAds.initialize(this@VibrationTesterActivity)
                setNativeAdsLoadBuilders()
                initializingApp = false
            }
            bottomNativeBannerAdLoader.loadAd(AdRequest.Builder().build())
            topNativeBannerAdLoader.loadAd(AdRequest.Builder().build())
            delay(60000L)
            loadAds()
        }
        loadAds!!.start()
    }

    private fun showTopAd() {
        if (showTopAd != null) showTopAd!!.cancel()
        showTopAd = lifecycleScope.launch {
            if (isFirstClick) {
                delay(1000L)
                showTopAd()
            } else {
                top_native_banner.visibility = View.VISIBLE
                showTopAd!!.cancel()
            }
        }
        showTopAd!!.start()
    }

    private fun showBottomAd() {
        if (showBottomAd != null) showBottomAd!!.cancel()
        showBottomAd = lifecycleScope.launch {
            if (isFirstClick) {
                delay(1000L)
                showBottomAd()
            } else {
                bottom_native_banner.visibility = View.VISIBLE
                showBottomAd!!.cancel()
            }
        }
        showBottomAd!!.start()
    }

    private fun setNativeAdsLoadBuilders() {
        topNativeBannerAdLoader =
            AdLoader.Builder(this@VibrationTesterActivity, topNativeAdUnitID)
                .forNativeAd { ad: NativeAd ->
                    if (!topNativeBannerAdLoader.isLoading) {
                        // Show the ad.
                        val styles =
                            NativeTemplateStyle.Builder()
                                .withMainBackgroundColor(ColorDrawable(0xFFFFFF)).build()
                        val template = findViewById<TemplateView>(R.id.top_native_banner)
                        template.setStyles(styles)
                        template.setNativeAd(ad)

                        showTopAd()
                    }

                    if (Build.VERSION.SDK_INT >= 17) {
                        if (isDestroyed) {
                            ad.destroy()
                            return@forNativeAd
                        }
                    } else {
                        listOfNativeAdsToDestroy.add(ad)
                    }
                }
                .withAdListener(object : AdListener() {
                    override fun onAdFailedToLoad(adError: LoadAdError) {
                        lifecycleScope.launch {
                            delay(5000L)
                            topNativeBannerAdLoader.loadAd(AdRequest.Builder().build())
                        }
                    }
                })
                .withNativeAdOptions(NativeAdOptions.Builder().build())
                .build()

        bottomNativeBannerAdLoader =
            AdLoader.Builder(this@VibrationTesterActivity, bottomNativeAdUnitID)
                .forNativeAd { ad: NativeAd ->
                    if (!bottomNativeBannerAdLoader.isLoading) {
                        // Show the ad.
                        val styles =
                            NativeTemplateStyle.Builder()
                                .withMainBackgroundColor(ColorDrawable(0xFFFFFF)).build()
                        val template = findViewById<TemplateView>(R.id.bottom_native_banner)
                        template.setStyles(styles)
                        template.setNativeAd(ad)

                        showBottomAd()
                    }

                    if (Build.VERSION.SDK_INT >= 17) {
                        if (isDestroyed) {
                            ad.destroy()
                            return@forNativeAd
                        }
                    } else {
                        listOfNativeAdsToDestroy.add(ad)
                    }
                }
                .withAdListener(object : AdListener() {
                    override fun onAdFailedToLoad(adError: LoadAdError) {
                        lifecycleScope.launch {
                            delay(5000L)
                            bottomNativeBannerAdLoader.loadAd(AdRequest.Builder().build())
                        }
                    }
                })
                .withNativeAdOptions(NativeAdOptions.Builder().build())
                .build()
    }
    //endregion

    override fun onCreate(savedInstancesState: Bundle?) {
        super.onCreate(savedInstancesState)
        setContentView(R.layout.activity_vibration_tester)

        lifecycleScope.launch {
            vib = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            play_button.setOnClickListener {

                if (isFirstClick) isFirstClick = false

                // troubleshooting if has vibrator on device
                if (vib.hasVibrator()) {
                    // troubleshooting API level
                    when {
                        Build.VERSION.SDK_INT < 26 -> {
                            // Switching play/pause
                            isNotVibrating = when (isNotVibrating) {
                                true -> {
                                    vib.vibrate(pattern, 0) // vibrate continuously
                                    play_button.setImageResource(R.drawable.pause)
                                    false
                                }
                                false -> {
                                    vib.cancel()
                                    play_button.setImageResource(R.drawable.play)
                                    true
                                }
                            }
                        }

                        Build.VERSION.SDK_INT >= 26 -> {

                            val amplitudeArray: IntArray = intArrayOf(0, 255, 0)
                            val defaultAmplitudeArray: IntArray =
                                intArrayOf(0, VibrationEffect.DEFAULT_AMPLITUDE, 0)

                            // Switching play/pause
                            isNotVibrating = when (isNotVibrating) {
                                true -> {
                                    vib.vibrate(
                                        VibrationEffect.createWaveform(
                                            pattern,
                                            if (vib.hasAmplitudeControl()) amplitudeArray
                                            else defaultAmplitudeArray,
                                            0
                                        )
                                    ) // vibrate continuously
                                    play_button.setImageResource(R.drawable.pause)
                                    false
                                }
                                false -> {
                                    vib.cancel()
                                    play_button.setImageResource(R.drawable.play)
                                    true
                                }
                            }
                        }
                    }
                } else
                    Toast.makeText(applicationContext, "No vibrator found.", Toast.LENGTH_SHORT)
                        .show()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        loadAds()
    }

    override fun onPause() {
        super.onPause()

        lifecycleScope.launch {
            loadAds!!.cancel()
            if (vib.hasVibrator()) {
                if (!isNotVibrating) {
                    vib.cancel()
                    play_button.setImageResource(R.drawable.play)
                    isNotVibrating = true
                }
            }
        }
    }

    override fun onStop() {
        super.onStop()

        lifecycleScope.launch {
            loadAds!!.cancel()
            if (vib.hasVibrator()) {
                if (!isNotVibrating) {
                    vib.cancel()
                    play_button.setImageResource(R.drawable.play)
                    isNotVibrating = true
                }
            }
        }
    }

    override fun onBackPressed() {
        lifecycleScope.launch {
            moveTaskToBack(true)
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        listOfNativeAdsToDestroy.forEach { ad ->
            try {
                ad.destroy()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}