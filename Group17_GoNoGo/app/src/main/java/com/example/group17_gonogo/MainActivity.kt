package com.example.group17_gonogo

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button

class MainActivity : AppCompatActivity() {

    private lateinit var startButton: Button
    private lateinit var instructionButton: Button
    private lateinit var exitButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        startButton = findViewById(R.id.start_button)
        instructionButton = findViewById(R.id.instruction_button)
        exitButton = findViewById(R.id.exit_button)
    }

    fun startTest(view: View) {
//        Log.i(TAG, "Entered startTest()")

        val mTestIntent = Intent(
            this@MainActivity,
            GoNoGoActivity::class.java)

        startActivity(mTestIntent)
    }

    fun showInstructions(view: View) {
//        Log.i(TAG, "Entered showInstructions()")
        //Show content of instructions
    }
    fun exitApp(view: View) {
        super.onBackPressed()
//        Log.i(TAG, "Exiting app")
    }

    companion object {
        private const val TAG = "GoNoGo"
    }
}