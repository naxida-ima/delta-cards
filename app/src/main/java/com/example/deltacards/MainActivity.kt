package com.example.deltacards

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Button
import android.widget.GridView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import org.json.JSONArray
import org.json.JSONObject

class MainActivity : Activity() {

    private lateinit var prefs: SharedPreferences
    private lateinit var grid: GridView
    private lateinit var progress: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var boardBox: LinearLayout
    private var server = ""
    private var token = ""
    private var username = ""
    private val collected = mutableSetOf<String>()

    private val ACCENT = Color.parseColor("#4F46E5")
    private val CARD_NORMAL = Color.parseColor("#FFFFFF")
    private val TEXT_DARK = Color.parseColor("#0F172A")
    private val TEXT_RED = Color.parseColor("#DC2626")
    private val TEXT_WHITE = Color.parseColor("#FFFFFF")

    data class Card(val key: String, val label: String, val red: Boolean)
    private val cards: List<Card> by lazy { buildCards() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        prefs = getSharedPreferences("deltacards", MODE_PRIVATE)
        server = prefs.getString("server", "") ?: ""
        token = prefs.getString("token", "") ?: ""
        username = prefs.getString("user", "") ?: ""

        if (token.isEmpty() || server.isEmpty()) { goLogin(); return }

        collected.addAll(prefs.getStringSet("collected", emptySet()) ?: emptySet())

        findViewById<TextView>(R.id.who).text = "已登录：$username"
        progress = findViewById(R.id.progress)
        progressBar = findViewById(R.id.progressBar)
        progressBar.max = cards.size
        boardBox = findViewById(R.id.boardBox)
        grid = findViewById(R.id.grid)
        grid.adapter = CardAdapter()

        grid.setOnItemClickListener { _, _, pos, _ ->
            val c = cards[pos]
            if (collected.contains(c.key)) collected.remove(c.key) else collected.add(c.key)
            saveLocal()
            (grid.adapter as CardAdapter).notifyDataSetChanged()
            updateProgress()
            syncProgress()
        }

        findViewById<Button>(R.id.reset).setOnClickListener {
            collected.clear(); saveLocal()
            (grid.adapter as CardAdapter).notifyDataSetChanged(); updateProgress(); syncProgress()
        }
        findViewById<Button>(R.id.btnRefresh).setOnClickListener { refreshAll() }
        findViewById<Button>(R.id.btnSwitch).setOnClickListener {
            prefs.edit().remove("token").apply(); goLogin()
        }

        updateProgress()
        refreshAll()
    }

    override fun onResume() {
        super.onResume()
        if (token.isNotEmpty()) refreshAll()
    }

    private fun goLogin() {
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }

    private fun saveLocal() {
        prefs.edit().putStringSet("collected", collected).apply()
    }

    private fun updateProgress() {
        val n = collected.size
        progress.text = "我的收集：$n / ${cards.size}　未收集 ${cards.size - n}"
        progressBar.progress = n
    }

    private fun refreshAll() {
        Thread {
            try {
                val me = Api.get(server, "sync", token)
                if (me.has("progress")) {
                    val arr = me.getJSONArray("progress")
                    val set = mutableSetOf<String>()
                    for (i in 0 until arr.length()) set.add(arr.getString(i))
                    this@MainActivity.runOnUiThread {
                        collected.clear(); collected.addAll(set); saveLocal()
                        (grid.adapter as CardAdapter).notifyDataSetChanged(); updateProgress()
                    }
                }
            } catch (_: Exception) { }
            try {
                val b = Api.get(server, "board", token)
                if (b.has("board")) {
                    val total = b.optInt("total", cards.size)
                    val arr = b.getJSONArray("board")
                    val rows = mutableListOf<Pair<String, Int>>()
                    for (i in 0 until arr.length()) {
                        val o = arr.getJSONObject(i)
                        rows.add(Pair(o.getString("user"), o.getInt("count")))
                    }
                    this@MainActivity.runOnUiThread { renderBoard(rows, total) }
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
            line.setPadding(0, 4, 0, 4)
            boardBox.addView(line)
        }
    }

    private fun syncProgress() {
        Thread {
            try {
                val arr = JSONArray()
                for (k in collected) arr.put(k)
                val body = JSONObject().apply { put("progress", arr) }
                Api.post(server, "sync", token, body)
            } catch (_: Exception) { }
        }.start()
    }

    private fun buildCards(): List<Card> {
        val suits = listOf(
            Triple("S", "♠", false),
            Triple("H", "♥", true),
            Triple("C", "♣", false),
            Triple("D", "♦", true)
        )
        val ranks = listOf("A", "2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K")
        val list = mutableListOf<Card>()
        for (s in suits) for (r in ranks) list.add(Card("${s.first}$r", "${s.second}$r", s.third))
        list.add(Card("JOKER_S", "小王", false))
        list.add(Card("JOKER_B", "大王", false))
        return list
    }

    inner class CardAdapter : BaseAdapter() {
        override fun getCount(): Int = cards.size
        override fun getItem(p: Int): Any = cards[p]
        override fun getItemId(p: Int): Long = p.toLong()
        override fun getView(p: Int, convertView: View?, parent: ViewGroup): View {
            val view = convertView ?: LayoutInflater.from(this@MainActivity)
                .inflate(R.layout.card_cell, parent, false)
            val card = view.findViewById<View>(R.id.card)
            val rank = view.findViewById<TextView>(R.id.rank)
            val check = view.findViewById<TextView>(R.id.check)
            val c = cards[p]
            val got = collected.contains(c.key)
            rank.text = c.label
            if (got) {
                card.backgroundTintList = ColorStateList.valueOf(ACCENT)
                rank.setTextColor(TEXT_WHITE)
                check.visibility = View.VISIBLE
            } else {
                card.backgroundTintList = ColorStateList.valueOf(CARD_NORMAL)
                rank.setTextColor(if (c.red) TEXT_RED else TEXT_DARK)
                check.visibility = View.GONE
            }
            return view
        }
    }
}
