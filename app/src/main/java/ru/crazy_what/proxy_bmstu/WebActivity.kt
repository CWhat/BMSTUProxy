package ru.crazy_what.proxy_bmstu

import android.annotation.SuppressLint
import android.os.Bundle
import android.webkit.HttpAuthHandler
import android.webkit.WebView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.webkit.ProxyConfig
import androidx.webkit.ProxyController
import androidx.webkit.WebViewClientCompat


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

        setProxy()

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

                if (url.contains(LOGIN_URL)) {
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
    private fun setProxy() {
        val proxyConfig: ProxyConfig =
            ProxyConfig.Builder().addProxyRule(PROXY_URL).addBypassRules(BYPASS_RULES)
                .setReverseBypassEnabled(true).build()
        ProxyController.getInstance().setProxyOverride(proxyConfig, { }) { }
    }

    companion object {
        // Можно добавлять другие сайты, но смысла в этом мало.
        // Эти сайты можно посмотреть тут: https://proxy.bmstu.ru
        private val BYPASS_RULES = listOf("*eu.bmstu.ru")

        private const val PROXY_HOST = "https://proxy.bmstu.ru"
        private const val PROXY_PORT = 8476
        private const val PROXY_URL = "$PROXY_HOST:$PROXY_PORT"
        private const val LOGIN_URL = "proxy.bmstu.ru:8443/cas/login"

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