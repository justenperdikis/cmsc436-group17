package com.example.group17_gonogo

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class GoNoGoActivity: AppCompatActivity() {

    private lateinit var viewToClick: TextView

    private lateinit var colorGreen: Color
    private lateinit var colorRed: Color

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.go_no_go)

        viewToClick = findViewById(R.id.block_four)

        // May add accessibility option for colorblind users -- if so, will need to change var names
        // because these colors will not be green and red
        colorGreen = Color.valueOf(ContextCompat.getColor(applicationContext, R.color.green))
        colorRed = Color.valueOf(ContextCompat.getColor(applicationContext, R.color.red))

        var isGreen = true

        viewToClick.setOnClickListener {
            isGreen = if (isGreen) {
                it.setBackgroundColor(colorRed.toArgb())
                false
            } else {
                it.setBackgroundColor(colorGreen.toArgb())
                true
            }
        }
    }

    companion object {
        const val TAG = "GoNoGo"
    }
}