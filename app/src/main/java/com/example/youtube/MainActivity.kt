package com.example.youtube

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
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
        val webSettings: WebSettings = myWebView.settings
        webSettings.javaScriptEnabled = true // Enable JavaScript if needed'

        handler = Handler(Looper.getMainLooper())

        myWebView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                swipeRefreshLayout.isRefreshing = false

            }
        }
        loadWebPage()

        swipeRefreshLayout.setOnRefreshListener {
            loadWebPage()
        }



    }

    private fun loadWebPage(){
        if (isNetworkAvailable()) {
            myWebView.webViewClient = WebViewClient() // Ensures links open within the WebView
            myWebView.loadUrl("https://www.youtube.com") // Replace with your URL

            handler.postDelayed({
                // Code to be executed after delay
                // Replace this with your function call
                swipeRefreshLayout.isRefreshing = false
            }, 5000)

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
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = connectivityManager.activeNetwork ?: return false
            val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false
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
}