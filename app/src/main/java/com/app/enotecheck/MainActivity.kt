package com.app.enotecheck

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowInsetsControllerCompat
import com.airbnb.lottie.LottieAnimationView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import kotlin.system.measureTimeMillis

class MainActivity : AppCompatActivity() {

    private val button by lazy { findViewById<LottieAnimationView>(R.id.lottieButton) }
    private val animation by lazy { findViewById<LottieAnimationView>(R.id.lottieAnimationView) }
    private val resultLayout by lazy { findViewById<LinearLayout>(R.id.result_layout) }
    private val imgResult by lazy { findViewById<ImageView>(R.id.img_result) }
    private val textResult by lazy { findViewById<TextView>(R.id.text_result) }
    private val buttonOpen by lazy { findViewById<Button>(R.id.button_open) }
    private val url = "https://enote.ofppt.ma/"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setupStatusBar()
        button.setOnClickListener { startProcess() }
        buttonOpen.setOnClickListener {
            openWebsite(url)
        }
    }

    private fun setupStatusBar() {
        val windowInsetsController = WindowInsetsControllerCompat(window, window.decorView)
        windowInsetsController.isAppearanceLightStatusBars = true
        window.statusBarColor = getColor(R.color.white)
    }

    private fun startProcess() {
        updateUI(isLoading = true)

        CoroutineScope(Dispatchers.IO).launch {
            val client = OkHttpClient()
            val request = Request.Builder().url(url).build()

            try {
                val responseTime = measureTimeMillis {
                    client.newCall(request).execute().use { response ->
                        processResponse(response)
                    }
                }
                Log.d("MainActivity", "Response Time: ${responseTime}ms")
            } catch (e: IOException) {
                showError(e)
            }
        }
    }

    private suspend fun processResponse(response: Response) {
        withContext(Dispatchers.Main) {
            if (response.isSuccessful) {
                showResult(R.drawable.verifier, R.string.result_ok, R.color.green, showOpenButton = true)
            } else {
                showResult(R.drawable.attention, R.string.result_internal_issue, R.color.yellow, showOpenButton = false)
            }
        }
    }

    private suspend fun showError(e: IOException) {
        Log.e("MainActivity", "Website is DOWN! Error: ${e.message}")
        withContext(Dispatchers.Main) {
            showResult(R.drawable.erreur, R.string.result_down, R.color.red, showOpenButton = false)
        }
    }

    private fun showResult(imageResId: Int, textResId: Int, textColorResId: Int, showOpenButton: Boolean) {
        Glide.with(this)
            .load(imageResId)
            .transition(DrawableTransitionOptions.withCrossFade())
            .into(imgResult)

        textResult.apply {
            setText(textResId)
            setTextColor(getColor(textColorResId))
            startAnimation(AnimationUtils.loadAnimation(this@MainActivity, R.anim.fade_in))
        }

        buttonOpen.visibility = if (showOpenButton) View.VISIBLE else View.GONE
        buttonOpen.startAnimation(AnimationUtils.loadAnimation(this, R.anim.fade_in))

        updateUI(isLoading = false)
    }

    private fun updateUI(isLoading: Boolean) {
        button.visibility = if (isLoading) View.GONE else View.VISIBLE
        animation.visibility = if (isLoading) View.VISIBLE else View.GONE
        resultLayout.visibility = if (isLoading) View.GONE else View.VISIBLE
    }
    private fun openWebsite(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivity(intent)
    }

}
