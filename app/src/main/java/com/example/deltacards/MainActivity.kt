package com.example.deltacards

import android.app.Activity
import android.content.Context
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
import android.widget.ProgressBar
import android.widget.TextView

class MainActivity : Activity() {

    private lateinit var prefs: SharedPreferences
    private lateinit var grid: GridView
    private lateinit var progress: TextView
    private lateinit var progressBar: ProgressBar
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

        prefs = getSharedPreferences("deltacards", Context.MODE_PRIVATE)
        collected.addAll(prefs.getStringSet("collected", emptySet()) ?: emptySet())

        progress = findViewById(R.id.progress)
        progressBar = findViewById(R.id.progressBar)
        progressBar.max = cards.size
        grid = findViewById(R.id.grid)
        grid.adapter = CardAdapter()

        grid.setOnItemClickListener { _, _, pos, _ ->
            val c = cards[pos]
            if (collected.contains(c.key)) collected.remove(c.key) else collected.add(c.key)
            save()
            (grid.adapter as CardAdapter).notifyDataSetChanged()
            updateProgress()
        }

        findViewById<Button>(R.id.reset).setOnClickListener {
            collected.clear()
            save()
            (grid.adapter as CardAdapter).notifyDataSetChanged()
            updateProgress()
        }

        updateProgress()
    }

    private fun save() {
        prefs.edit().putStringSet("collected", collected).apply()
    }

    private fun updateProgress() {
        val n = collected.size
        val total = cards.size
        progress.text = "已收集 $n / $total　未收集 ${total - n}"
        progressBar.progress = n
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
