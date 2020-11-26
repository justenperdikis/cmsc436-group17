package com.example.group17_gonogo

import android.app.Activity
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.widget.TextView
import androidx.annotation.RequiresApi
import java.util.ArrayList

class TestResultPopUp : Activity() {

    private lateinit var resultTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.test_result_pop_up)
        Log.i(TAG, "Entered pop up onCreate")

        resultTextView = findViewById(R.id.result_text_view)

        window.setLayout(1000, 1500)                                        // set the size of the popup window, might need to adjust later based on the number of test done

        var windowParams = window.attributes
        windowParams.gravity = Gravity.CENTER                                            // set the pop up window to appear in the center of the screen

        window.attributes = windowParams

        showResult(intent.getSerializableExtra("result") as ArrayList<GNGResult>)
    }

    private fun showResult(list: ArrayList<GNGResult>) {
        // Log.i(TAG, "showResult() called with list size of ${list.size}")
        var resultText = ""

        // loop through the list, create resultString based on the value on each result, append it to create one long string
        for (result in list) {
            var singleResult = "${result.getGNGMode()} - ${result.getReactTime()} ms - ${result.getTestStatus()}\n"
            resultText += singleResult
        }

        resultTextView.text = resultText
    }

    companion object {
        const val TAG = "GoNoGo"
    }
}