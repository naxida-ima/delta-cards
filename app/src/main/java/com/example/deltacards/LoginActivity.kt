package com.example.deltacards

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.text.TextUtils
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import org.json.JSONObject

class LoginActivity : Activity() {

    private lateinit var prefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        prefs = getSharedPreferences("deltacards", MODE_PRIVATE)

        // 自动登录：本地已存有效会话则直接进入主界面，无需重新登录
        val savedServer = prefs.getString("server", "") ?: ""
        val savedToken = prefs.getString("token", "") ?: ""
        if (savedServer.isNotEmpty() && savedToken.isNotEmpty()) {
            startMain()
            return
        }

        val serverEd = findViewById<EditText>(R.id.server)
        val userEd = findViewById<EditText>(R.id.user)
        val passEd = findViewById<EditText>(R.id.pass)
        val loginBtn = findViewById<Button>(R.id.login)
        val regBtn = findViewById<Button>(R.id.register)
        val msg = findViewById<TextView>(R.id.msg)

        val saved = prefs.getString("server", "")
        if (!TextUtils.isEmpty(saved)) serverEd.setText(saved)

        loginBtn.setOnClickListener {
            val s = normalizeServer(serverEd.text.toString())
            val u = userEd.text.toString().trim()
            val p = passEd.text.toString()
            if (s.isEmpty() || u.isEmpty() || p.isEmpty()) {
                msg.text = "请把服务器 / 账号 / 密码填全"
                return@setOnClickListener
            }
            msg.text = "登录中…"
            Thread {
                try {
                    val body = JSONObject().apply { put("user", u); put("pass", p) }
                    val res = Api.post(s, "login", null, body)
                    if (res.has("token")) {
                        saveSession(s, u, res.getString("token"))
                        this@LoginActivity.runOnUiThread { startMain() }
                    } else {
                        this@LoginActivity.runOnUiThread { msg.text = res.optString("error", "登录失败") }
                    }
                } catch (e: Exception) {
                    this@LoginActivity.runOnUiThread { msg.text = "网络错误：${e.message}" }
                }
            }.start()
        }

        regBtn.setOnClickListener {
            val s = normalizeServer(serverEd.text.toString())
            val u = userEd.text.toString().trim()
            val p = passEd.text.toString()
            if (s.isEmpty() || u.isEmpty() || p.isEmpty()) {
                msg.text = "请把服务器 / 账号 / 密码填全"
                return@setOnClickListener
            }
            msg.text = "注册中…"
            Thread {
                try {
                    val body = JSONObject().apply { put("user", u); put("pass", p) }
                    val res = Api.post(s, "register", null, body)
                    if (res.has("token")) {
                        saveSession(s, u, res.getString("token"))
                        this@LoginActivity.runOnUiThread { startMain() }
                    } else {
                        this@LoginActivity.runOnUiThread { msg.text = res.optString("error", "注册失败") }
                    }
                } catch (e: Exception) {
                    this@LoginActivity.runOnUiThread { msg.text = "网络错误：${e.message}" }
                }
            }.start()
        }
    }

    private fun normalizeServer(raw: String): String {
        var s = raw.trim().trimEnd('/')
        if (s.isEmpty()) return ""
        if (!s.startsWith("http://") && !s.startsWith("https://")) s = "http://$s"
        return if (s.endsWith("api.php")) s else "$s/api.php"
    }

    private fun saveSession(server: String, user: String, token: String) {
        prefs.edit().putString("server", server).putString("user", user).putString("token", token).apply()
    }

    private fun startMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
