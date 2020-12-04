package com.example.group17gonogo

import android.annotation.SuppressLint
import android.app.Activity
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView

class ScoreList(private val context:Activity, private var scores: List<Score>) :
    ArrayAdapter<Score>(context, R.layout.score_list_layout, scores) {

    @SuppressLint("InflateParams", "ViewHolder")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        // Log.i(TAG, "entered getView in ScoreList")
        val inflater = context.layoutInflater
        val listViewItem = inflater.inflate(R.layout.score_list_layout, null, true)

        val username = listViewItem.findViewById<View>(R.id.score_username) as TextView
        val scoreValue = listViewItem.findViewById<View>(R.id.score_value) as TextView

        val score = scores[position]
        username.text = score.userId
        scoreValue.text = score.score.toString()

        return listViewItem
    }

    companion object {
        val TAG = "GoNoGo"
    }
}