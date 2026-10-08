package com.muzikdolabi.app

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.webkit.JavascriptInterface
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.webkit.WebViewAssetLoader
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : Activity() {
    private lateinit var web: WebView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val yonetici = BagimsizVeriYonetici(applicationContext)
        val loader = WebViewAssetLoader.Builder()
            .addPathHandler("/assets/", WebViewAssetLoader.AssetsPathHandler(this)).build()

        web = WebView(this)
        setContentView(web)
        web.settings.javaScriptEnabled = true
        web.settings.domStorageEnabled = true
        web.webViewClient = object : WebViewClient() {
            override fun shouldInterceptRequest(v: WebView, r: WebResourceRequest) =
                loader.shouldInterceptRequest(r.url)

            // Spotify / YouTube bağlantıları telefondaki uygulamada veya tarayıcıda açılır
            override fun shouldOverrideUrlLoading(v: WebView, r: WebResourceRequest): Boolean {
                if (r.url.host == "appassets.androidplatform.net") return false
                startActivity(Intent(Intent.ACTION_VIEW, r.url))
                return true
            }
        }
        web.addJavascriptInterface(object {
            @JavascriptInterface fun veri(): String = yonetici.kutuphaneyiOku()
        }, "Android")
        web.loadUrl("https://appassets.androidplatform.net/assets/index.html")

        // Açılışı geciktirmeden arka planda güncelleme denetimi
        CoroutineScope(Dispatchers.IO).launch { yonetici.sunucudanGuncellemeDenetle() }
    }

    @Deprecated("Geri tuşu")
    override fun onBackPressed() { if (web.canGoBack()) web.goBack() else super.onBackPressed() }
}
