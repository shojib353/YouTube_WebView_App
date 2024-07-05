package com.example.youtube

import android.content.Context
import android.content.pm.ActivityInfo
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout

class MainActivity : AppCompatActivity() {

    private lateinit var myWebView: WebView
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var handler:Handler
    private lateinit var mainContainer: FrameLayout
    private lateinit var fullscreenContainer: FrameLayout

    private var customView: View? = null
    private var customViewCallback: WebChromeClient.CustomViewCallback? = null
    private var isFullscreen: Boolean = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }


        myWebView = findViewById(R.id.webview)
        swipeRefreshLayout = findViewById(R.id.swipeContainer)
        mainContainer = findViewById(R.id.main_container)
        fullscreenContainer = findViewById(R.id.fullscreen_container)

        val webSettings: WebSettings = myWebView.settings
        webSettings.javaScriptEnabled = true // Enable JavaScript if needed'

        handler = Handler(Looper.getMainLooper())

        myWebView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                swipeRefreshLayout.isRefreshing = false

            }
        }



        myWebView.webChromeClient = object : WebChromeClient() {
            override fun onShowCustomView(view: View?, callback: CustomViewCallback?) {
                if (customView != null) {
                    callback?.onCustomViewHidden()
                    return
                }

                customView = view
                customViewCallback = callback

                fullscreenContainer.addView(customView)
                fullscreenContainer.visibility = View.VISIBLE
                mainContainer.visibility = View.GONE
                swipeRefreshLayout.visibility = View.GONE


                isFullscreen = true
                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                hideSystemUI()

                customView?.layoutParams = FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT
                )

            }

            override fun onHideCustomView() {
                if (customView == null) {
                    return
                }

                fullscreenContainer.removeView(customView)
                customView = null
                fullscreenContainer.visibility = View.GONE
                mainContainer.visibility = View.VISIBLE
                swipeRefreshLayout.visibility = View.VISIBLE

                customViewCallback?.onCustomViewHidden()

                isFullscreen = false
                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

                showSystemUI()
            }
        }



        loadWebPage("https://www.youtube.com")

        swipeRefreshLayout.setOnRefreshListener {
            refreshCurrentPage()
        }



    }

    private fun refreshCurrentPage() {
        val currentUrl = myWebView.url
        if (isNetworkAvailable()) {
            myWebView.reload()
        } else {
            Toast.makeText(this, "No internet connection", Toast.LENGTH_LONG).show()
            myWebView.loadUrl("file:///android_asset/offline.html")
            swipeRefreshLayout.isRefreshing = false
        }
    }


    private fun hideSystemUI() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.let {
                it.hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
                it.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } else {
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
        }
    }

    private fun showSystemUI() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.show(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
        } else {
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_VISIBLE)
        }
    }



    private fun loadWebPage(url: String) {
        if (isNetworkAvailable()) {
            myWebView.loadUrl(url)
             handler.postDelayed({

                swipeRefreshLayout.isRefreshing = false
            }, 3000)

        } else {
            Toast.makeText(this, "No internet connection", Toast.LENGTH_LONG).show()
            // Optionally, you can load a local HTML file or a custom offline page
            myWebView.loadUrl("file:///example/offline.html")
            swipeRefreshLayout.isRefreshing = false
        }
    }



    override fun onBackPressed() {
        if (myWebView.canGoBack()) {
            myWebView.goBack()
        } else {
            super.onBackPressed()
        }
    }




        private fun isNetworkAvailable(): Boolean {
            val connectivityManager =
                getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val network = connectivityManager.activeNetwork ?: return false
                val activeNetwork =
                    connectivityManager.getNetworkCapabilities(network) ?: return false
                return when {
                    activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                    activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                    else -> false
                }
            } else {
                @Suppress("DEPRECATION")
                val networkInfo = connectivityManager.activeNetworkInfo
                return networkInfo != null && networkInfo.isConnected
            }
        }

            override fun onConfigurationChanged(newConfig: android.content.res.Configuration) {
                super.onConfigurationChanged(newConfig)
                if (isFullscreen && newConfig.orientation != android.content.res.Configuration.ORIENTATION_LANDSCAPE)
                {
                    requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                }

            }
    }
