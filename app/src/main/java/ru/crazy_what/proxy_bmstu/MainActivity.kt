package ru.crazy_what.proxy_bmstu

import android.annotation.SuppressLint
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys


class MainActivity : AppCompatActivity() {

    private lateinit var loginEditText: EditText
    private lateinit var passwordEditText: EditText

    // Последние сохраненные логин и пароль
    private lateinit var lastLogin: String
    private lateinit var lastPassword: String

    private val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
    private lateinit var sharedPreferences: SharedPreferences

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        supportActionBar?.hide()

        sharedPreferences = EncryptedSharedPreferences.create(
            SHARED_PREF_NAME,
            masterKeyAlias,
            this,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )

        loginEditText = findViewById(R.id.login)
        passwordEditText = findViewById(R.id.password)

        lastLogin = sharedPreferences.getString(LOGIN_PREF, "")!!
        lastPassword = sharedPreferences.getString(PASSWORD_PREF, "")!!

        loginEditText.setText(lastLogin)
        passwordEditText.setText(lastPassword)
    }

    @Suppress("UNUSED_PARAMETER")
    fun openWebActivity(view: View) {
        val login = loginEditText.text.toString()
        val password = passwordEditText.text.toString()

        val intent = Intent(applicationContext, WebActivity::class.java)
        intent.putExtra(WebActivity.USERNAME_EXTRA, login)
        intent.putExtra(WebActivity.PASSWORD_EXTRA, password)

        // Сохраняем логин и пароль, если они изменились
        val editor = sharedPreferences.edit()
        if (login != lastLogin)
            editor.putString(LOGIN_PREF, login)
        if (password != lastPassword)
            editor.putString(PASSWORD_PREF, password)
        editor.apply()

        startActivity(intent)
    }

    companion object {
        private const val SHARED_PREF_NAME = "secret_shared_prefs"
        private const val LOGIN_PREF = "login"
        private const val PASSWORD_PREF = "password"
    }
}