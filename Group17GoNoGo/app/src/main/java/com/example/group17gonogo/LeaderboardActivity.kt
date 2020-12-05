package com.example.group17gonogo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ListView
import com.google.firebase.database.*
import java.lang.Exception

class LeaderboardActivity : AppCompatActivity() {

    //private lateinit var scoreDatabase: DatabaseReference

    private lateinit var scoreDatabase: Query
    lateinit var scoreListView: ListView
    lateinit var scores: MutableList<Score>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_leaderboard)

        scoreDatabase = FirebaseDatabase.getInstance().getReference("scores").orderByChild("score").limitToLast(50)
        scoreListView = findViewById(R.id.score_list_view)

        scoreListView.isClickable = false

        scores = ArrayList()

    }

    override fun onStart() {
        super.onStart()

        scoreDatabase.addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                Log.i(TAG, "onDataChanged in onStart called")
                scores.clear()
                var score: Score? = null

                for (dataSnapshot in snapshot.children) {
                    try {
                        score = dataSnapshot.getValue(Score::class.java)
                    } catch (e: Exception) {
                        Log.e(TAG, e.toString())
                    } finally {
                        scores.add(score!!)
                    }
                }

                val authorAdapter = ScoreList(this@LeaderboardActivity, scores)
                scoreListView.adapter = authorAdapter
            }

            override fun onCancelled(error: DatabaseError) {
                // do nothing
            }
        })
    }

    companion object {
        val TAG = "GoNoGo"
    }
}