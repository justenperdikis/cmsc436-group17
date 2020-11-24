package com.example.group17_gonogo

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class GoNoGoActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.go_no_go)
    }

    companion object {
        const val TAG = "GoNoGo"
    }
}