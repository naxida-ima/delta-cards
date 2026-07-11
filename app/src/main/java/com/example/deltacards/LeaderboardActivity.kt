package com.example.deltacards

import android.app.Activity
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import org.json.JSONArray
import org.json.JSONObject

class LeaderboardActivity : Activity() {

    private lateinit var prefs: SharedPreferences
    private lateinit var boardBox: LinearLayout
    private var server = ""
    private var token = ""
    private var username = ""

    private val ACCENT = Color.parseColor("#4F46E5")
    private val TEXT_DARK = Color.parseColor("#0F172A")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_leaderboard)

        prefs = getSharedPreferences("deltacards", MODE_PRIVATE)
        server = prefs.getString("server", "") ?: ""
        token = prefs.getString("token", "") ?: ""
        username = prefs.getString("user", "") ?: ""

        boardBox = findViewById(R.id.boardBox)

        findViewById<Button>(R.id.btnBack).setOnClickListener { finish() }
        findViewById<Button>(R.id.btnRefresh).setOnClickListener { loadBoard() }
        findViewById<Button>(R.id.reset).setOnClickListener { doReset() }

        loadBoard()
    }

    private fun loadBoard() {
        Thread {
            try {
                val b = Api.get(server, "board", token)
                if (b.has("board")) {
                    val total = b.optInt("total", 54)
                    val arr = b.getJSONArray("board")
                    val rows = mutableListOf<Pair<String, Int>>()
                    for (i in 0 until arr.length()) {
                        val o = arr.getJSONObject(i)
                        rows.add(Pair(o.getString("user"), o.getInt("count")))
                    }
                    runOnUiThread { renderBoard(rows, total) }
                }
            } catch (_: Exception) { }
        }.start()
    }

    private fun renderBoard(rows: List<Pair<String, Int>>, total: Int) {
        boardBox.removeAllViews()
        if (rows.isEmpty()) {
            val t = TextView(this)
            t.text = "全服进度：还没有人开始收集"
            t.setTextColor(Color.parseColor("#64748B"))
            boardBox.addView(t)
            return
        }
        for ((u, cnt) in rows) {
            val line = TextView(this)
            line.text = "$u   $cnt/$total"
            line.textSize = 14f
            line.setTextColor(if (u == username) ACCENT else TEXT_DARK)
            line.setPadding(0, 6, 0, 6)
            boardBox.addView(line)
        }
    }

    private fun doReset() {
        // 清空本地收集 + 上报空进度；返回主界面后由 onResume 重新同步刷新
        prefs.edit().putStringSet("collected", emptySet()).apply()
        Thread {
            try {
                val body = JSONObject().apply { put("progress", JSONArray()) }
                Api.post(server, "sync", token, body)
            } catch (_: Exception) { }
            runOnUiThread { finish() }
        }.start()
    }
}
