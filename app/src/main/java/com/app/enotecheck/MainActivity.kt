package com.app.enotecheck

import android.graphics.Color
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.view.WindowInsetsControllerCompat
import com.airbnb.lottie.LottieAnimationView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okio.IOException
import kotlin.system.measureTimeMillis

class MainActivity : AppCompatActivity() {

    private lateinit var button: LottieAnimationView
    private lateinit var animation: LottieAnimationView
    private lateinit var resultLayout: LinearLayout
    private lateinit var imgResult: ImageView
    private lateinit var textResult: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initViews()
        setupStatusBar()

        button.setOnClickListener {
            startProcess()
        }
    }

    private fun initViews() {
        button = findViewById(R.id.lottieButton)
        animation = findViewById(R.id.lottieAnimationView)
        resultLayout = findViewById(R.id.result_layout)
        imgResult = findViewById(R.id.img_result)
        textResult = findViewById(R.id.text_result)
    }

    private fun setupStatusBar() {
        val windowInsetsController = WindowInsetsControllerCompat(window, window.decorView)
        windowInsetsController.isAppearanceLightStatusBars = true
        window.statusBarColor = resources.getColor(R.color.white, null)
    }

    private fun startProcess() {
        button.visibility = View.GONE
        resultLayout.visibility = View.GONE
        animation.visibility = View.VISIBLE

        CoroutineScope(Dispatchers.IO).launch {
            val client = OkHttpClient()
            val request = Request.Builder().url("https://enote.ofppt.ma/").build()

            try {
                val responseTime = measureTimeMillis {
                    client.newCall(request).execute().use { response ->
                        handleResponse(response)
                    }
                }
                println("Response Time: ${responseTime}ms")
            } catch (e: IOException) {
                handleError(e)
            }
        }
    }

    private suspend fun handleResponse(response: okhttp3.Response) {
        withContext(Dispatchers.Main) {
            when {
                response.isSuccessful -> {
                    showResult(R.drawable.verifier, R.string.result_ok, R.color.green)
                }
                else -> {
                    showResult(R.drawable.attention, R.string.result_intrernal_issue, R.color.yellow)
                }
            }
            button.visibility = View.VISIBLE
            animation.visibility = View.GONE
            resultLayout.visibility = View.VISIBLE
        }
    }

    private suspend fun handleError(e: IOException) {
        withContext(Dispatchers.Main) {
            showResult(R.drawable.erreur, R.string.result_down, R.color.red)
            println("Website is DOWN! Error: ${e.message}")
            button.visibility = View.VISIBLE
            animation.visibility = View.GONE
            resultLayout.visibility = View.VISIBLE
        }
    }

    private fun showResult(imageResId: Int, textResId: Int, textColorResId: Int) {
        Glide.with(this@MainActivity)
            .load(imageResId)
            .transition(DrawableTransitionOptions.withCrossFade())
            .into(imgResult)

        textResult.setTextColor(getColor(textColorResId))
        textResult.setText(textResId)

        val fadeIn = AnimationUtils.loadAnimation(this@MainActivity, R.anim.fade_in)
        textResult.startAnimation(fadeIn)
    }
}
