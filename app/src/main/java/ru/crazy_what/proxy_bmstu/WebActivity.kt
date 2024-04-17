package ru.crazy_what.proxy_bmstu

import android.R.attr.mimeType
import android.annotation.SuppressLint
import android.app.DownloadManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.webkit.CookieManager
import android.webkit.HttpAuthHandler
import android.webkit.URLUtil
import android.webkit.WebView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.webkit.ProxyConfig
import androidx.webkit.ProxyController
import androidx.webkit.WebViewClientCompat


// TODO:
//  1. Настроить тему приложения
//  2. Разобраться с минимальной версией Android
//  3. Настроить релизную сборку

// TODO дополнительно:
//  1. Реализовать загрузку файлов
//  2. Сделать кнопки назад, вперед, адресную строку
//  3. Сделать кнопку обновления
//  4. Добавить анимацию загрузки сайта (какую-нибудь строку с прогрессом сверху или снизу)
//  5. Оформить ресурсы приложения

// TODO нафиг нужно, но не помешало бы:
//  1. Поиск по странице
//  2. Закладки
//  3. Сделать выбор темной темы (она видна только на экране ввода данных пользователя)
//  4. Оформить ошибки WebView (например, ошибка сети и прочее)

class WebActivity : AppCompatActivity() {

    private lateinit var webview: WebView
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_web)
        supportActionBar?.hide()

        val username = intent.getStringExtra(USERNAME_EXTRA)
        val password = intent.getStringExtra(PASSWORD_EXTRA)

        if (username == null) {
            Toast.makeText(applicationContext, "Не указан логин!", Toast.LENGTH_LONG).show()
            finish()
        }

        if (password == null) {
            Toast.makeText(applicationContext, "Не указан пароль!", Toast.LENGTH_LONG).show()
            finish()
        }

        webview = findViewById(R.id.webview)
        swipeRefreshLayout = findViewById(R.id.container)

        setProxy(proxyHost, proxyPort)

        //webview.settings.setUserAgentString(ua)
        webview.settings.javaScriptEnabled = true
        webview.settings.domStorageEnabled = true

        webview.webViewClient = object : WebViewClientCompat() {
            override fun onReceivedHttpAuthRequest(
                view: WebView,
                handler: HttpAuthHandler,
                host: String,
                realm: String,
            ) {
                handler.proceed(username, password)
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                if (view == null || url == null) return

                swipeRefreshLayout.isRefreshing = false

                if (url.contains("proxy.bmstu.ru:8443/cas/login")) {
                    // Заполняем поля и делаем кнопку "ВОЙТИ" активной
                    // Как сделать автоматический переход, я не знаю
                    view.loadUrl(
                        "javascript:(function() {" +
                                "document.getElementById('username').value = '$username';" +
                                "document.getElementById('password').value = '$password';" +
                                "document.querySelectorAll('[type=\"submit\"]')[0].removeAttribute(\"disabled\");" +
                                " ;})()"
                    )
                }
            }
        }

        ////
        /*webview.setDownloadListener {
                url, userAgent, contentDisposition, mimetype: String,
                contentLength: Long,
            ->
            val title = URLUtil.guessFileName(url, contentDisposition, mimetype)

            //Log.d("MyLog", "title = $title, cookies = $cookies")
            //Log.d(
            //    "MyLog",
            //    "url = $url, userAgent = $userAgent, contentDisposition = $contentDisposition, mimetype = $mimetype, contentLength = $contentLength"
            //)

            val request = DownloadManager.Request(Uri.parse(url))
            request.setMimeType(mimetype)
            val cookies = CookieManager.getInstance().getCookie(url)


            request.addRequestHeader("cookie", cookies)
            request.addRequestHeader("User-Agent", userAgent)
            request.setDescription("Downloading file...")

            request.setTitle(title)
            request.allowScanningByMediaScanner()
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            request.setDestinationInExternalPublicDir(
                Environment.DIRECTORY_DOWNLOADS, URLUtil.guessFileName(
                    url, contentDisposition, mimetype
                )
            )
            val dm = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
            dm.enqueue(request)
            Toast.makeText(
                applicationContext, "Downloading File",
                Toast.LENGTH_LONG
            ).show()
        }*/
        ////

        webview.loadUrl("https://eu.bmstu.ru/")

        swipeRefreshLayout.setOnRefreshListener {
            webview.reload()
        }
    }

    override fun onBackPressed() {
        if (webview.canGoBack()) webview.goBack()
        else super.onBackPressed()
    }

    @SuppressLint("RequiresFeature")
    private fun setProxy(host: String, port: Int) {
        val proxyUrl = "${host}:${port}"
        val proxyConfig: ProxyConfig =
            ProxyConfig.Builder().addProxyRule(proxyUrl).addBypassRules(BYPASS_RULES)
                .setReverseBypassEnabled(true).build()
        ProxyController.getInstance().setProxyOverride(proxyConfig, { }) { }
    }

    companion object {
        // Можно добавлять другие сайты, но смысла в этом мало.
        // Эти сайты можно посмотреть тут: https://proxy.bmstu.ru
        private val BYPASS_RULES = listOf("*eu.bmstu.ru")

        private const val proxyHost = "https://proxy.bmstu.ru"
        private const val proxyPort = 8476

        private fun ProxyConfig.Builder.addBypassRules(rules: List<String>): ProxyConfig.Builder {
            for (rule in rules) {
                this.addBypassRule(rule)
            }
            return this
        }

        const val USERNAME_EXTRA = "username"
        const val PASSWORD_EXTRA = "password"
    }
}