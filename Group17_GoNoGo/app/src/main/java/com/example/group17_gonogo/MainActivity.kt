package com.example.group17_gonogo

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val startButton = findViewById(R.id.start_button) as Button
        val instructionButton = findViewById(R.id.instruction_button) as Button
        val exitButton = findViewById(R.id.exit_button) as Button
    }

    fun startTest(view: View) {

        val mTestIntent = Intent(this, "activity name"::class.java);
        startActivity(mTestIntent);

    }

    fun showInstrunctions(view: View) {
        //Show content of instructions
    }
    fun exitApp(view: View) {
        super.onBackPressed()
    }
}