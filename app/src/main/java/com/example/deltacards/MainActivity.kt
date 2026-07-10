package com.example.deltacards

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Button
import android.widget.GridView
import android.widget.TextView

class MainActivity : Activity() {

    private lateinit var prefs: SharedPreferences
    private lateinit var grid: GridView
    private lateinit var progress: TextView
    private val collected = mutableSetOf<String>()

    data class Card(val key: String, val label: String, val red: Boolean)

    private val cards: List<Card> by lazy { buildCards() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        prefs = getSharedPreferences("deltacards", Context.MODE_PRIVATE)
        collected.addAll(prefs.getStringSet("collected", emptySet()) ?: emptySet())

        progress = findViewById(R.id.progress)
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
        progress.text = "已收集 ${collected.size} / ${cards.size}　未收集 ${cards.size - collected.size}"
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
            val tv = (convertView as? TextView) ?: TextView(this@MainActivity).apply {
                val pad = (16 * resources.displayMetrics.density).toInt()
                setPadding(pad, pad, pad, pad)
                textSize = 20f
                gravity = Gravity.CENTER
            }
            val c = cards[p]
            val got = collected.contains(c.key)
            tv.text = if (got) "${c.label} ✓" else c.label
            tv.setTextColor(if (c.red) Color.parseColor("#d00000") else Color.BLACK)
            tv.setBackgroundColor(if (got) Color.parseColor("#c8f7c5") else Color.parseColor("#eeeeee"))
            return tv
        }
    }
}
