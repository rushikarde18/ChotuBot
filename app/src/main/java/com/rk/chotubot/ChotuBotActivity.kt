package com.rk.chotubot

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.Bundle
import android.webkit.*
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

import com.rk.chotubot.utils.Progress

class ChotuBotActivity : AppCompatActivity(){
    private val TAG = "ChotuBotActivity"
    private var webURL = "https://bot.dialogflow.com/8cf0828b-45c4-44e6-8eb1-77eaec152cc1" // Change it with your URL
    private lateinit var webView : WebView

    private var progress: Progress? = null
    private var isLoaded: Boolean = false



    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_chotu_bot)
        webView = findViewById(R.id.webView)

        webView.settings.javaScriptEnabled = true

        setDesktopMode(webView, true)

        if (!isLoaded) loadWebView()

    }
   /* override fun onResume() {
        if (!isLoaded) loadWebView()
        super.onResume()
    }
*/
    private fun loadWebView() {
        webView.loadUrl(webURL)
       loadJs(webView)
        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                val url = request?.url.toString()
                view?.loadUrl(url)
                return super.shouldOverrideUrlLoading(view, request)
            }

            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                setProgressDialogVisibility(true)
                super.onPageStarted(view, url, favicon)
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                isLoaded = true
                setProgressDialogVisibility(false)
                super.onPageFinished(view, url)
            }

            override fun onReceivedError(view: WebView, request: WebResourceRequest, error: WebResourceError) {
                isLoaded = false
                val errorMessage = "Got Error! $error"
                Toast.makeText(this@ChotuBotActivity,errorMessage, Toast.LENGTH_SHORT).show()
                setProgressDialogVisibility(false)
                super.onReceivedError(view, request, error)
            }
        }
    }

    private fun setProgressDialogVisibility(visible: Boolean) {
        if (visible) progress = Progress(this, R.string.please_wait, cancelable = true)
        progress?.apply { if (visible) show() else dismiss() }
    }

    fun setDesktopMode(webView: WebView, enabled: Boolean) {

        var newUserAgent = webView.settings.userAgentString
        webView.getSettings().setLoadWithOverviewMode(true);
        webView.getSettings().setUseWideViewPort(true);

        webView.getSettings().setSupportZoom(true);
        webView.getSettings().setBuiltInZoomControls(true);
        webView.getSettings().setDisplayZoomControls(false);

        webView.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
        webView.setScrollbarFadingEnabled(false);

        if (enabled) {
            try {
                val ua = webView.settings.userAgentString
                val androidOSString =
                    webView.settings.userAgentString.substring(ua.indexOf("("), ua.indexOf(")") + 1)
                newUserAgent =
                    webView.settings.userAgentString.replace(androidOSString, "(X11; Linux x86_64)")
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else {
            newUserAgent = null
        }
        webView.settings.userAgentString = newUserAgent
        webView.settings.useWideViewPort = enabled
        webView.settings.loadWithOverviewMode = enabled
        webView.reload()
    }


    private fun loadJs(webView: WebView) {
        webView.evaluateJavascript("<script src=\"https://www.gstatic.com/dialogflow-console/fast/messenger/bootstrap.js?v=1\"></script>\n" +
                "<df-messenger\n" +
                "  intent=\"WELCOME\"\n" +
                "  chat-title=\"Chotu\"\n" +
                "  agent-id=\"8cf0828b-45c4-44e6-8eb1-77eaec152cc1\"\n" +
                "  language-code=\"en\"\n" +
                "></df-messenger>", null)

      /*  webView.loadUrl(
            """javascript:(function f() {
        var btns = document.getElementsByTagName('button');
        for (var i = 0, n = btns.length; i < n; i++) {
          if (btns[i].getAttribute('aria-label') === 'Support') {
            btns[i].setAttribute('onclick', 'Android.onClicked()');
          }
        }
      })()"""
        )*/
    }

   /* fun checkPermissions() {
        if (ContextCompat.checkSelfPermission(
                this@ChotuBotActivity,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            ActivityCompat.requestPermissions(
                this@ChotuBotActivity,
                new String []{ Manifest.permission.RECORD_AUDIO },
                REQUEST_MICROPHONE
            );

        }
    }*/
/*

    private fun injectToIframe(url: String): WebResourceResponse? {
        Log.d(TAG, "Intercepted $url")
        val latch = CountDownLatch(1)
        var res: InputStream? = null
        val call = App.instance?.okHttpClient?.newCall(DownloadManager.Request.Builder().url(url).build())
        call?.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                latch.countDown()
            }

            override fun onResponse(call: Call, response: Response) {
                res = response.body?.byteStream()
                latch.countDown()
            }
        })

        latch.await()

        val reader = BufferedReader(res?.reader())
        var content: String
        try {
            content = reader.readText()
        } finally {
            reader.close()
        }

        var scriptToInject = "\n&lt;script>\n" +
                "   (function() {\n" +
                "     alert('hi');\n" +
                "   })()\n" +
                "&lt;/script>\n"
        val newContent = "${content.split("&lt;/head>")[0]}${scriptToInject}</head>${content.split("&lt;/head>")[1]}"
        Log.d(TAG, "Inject script to iframe: $newContent")
        val inStream = newContent.byteInputStream()
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) return WebResourceResponse(
            "text/html",
            "utf-8",
            inStream
        )
        val statusCode = 200
        val reasonPhase = "OK"
        val responseHeaders: MutableMap<String, String> = HashMap()
        return WebResourceResponse("text/html", "utf-8", statusCode, reasonPhase, responseHeaders, inStream)
    }

    override fun shouldInterceptRequest(view: WebView?, url: String?): WebResourceResponse? {
        if (shouldInjectToIframe(url)) return injectToIframe(url!!)
        if (Util.SDK_INT &lt; Build.VERSION_CODES.LOLLIPOP) return super.shouldInterceptRequest(view, url)
        return null
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun shouldInterceptRequest(view: WebView?, request: WebResourceRequest): WebResourceResponse? {
        if (shouldInjectToIframe(request.url.toString())) return injectToIframe(request.url.toString())
        return super.shouldInterceptRequest(view, request)
    }*/
}