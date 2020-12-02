package com.example.group17gonogo

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class InstructionsActivity : AppCompatActivity() {
    private lateinit var mConditionTextView: TextView
    private lateinit var mButtonSunny: Button
    private lateinit var mButtonFoggy: Button

    private val mRootRef = FirebaseDatabase.getInstance().getReference()
    private val mConditionRef = mRootRef.child("condition")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_instructions)

        mConditionTextView = findViewById(R.id.textViewCondition)
        mButtonSunny = findViewById(R.id.buttonSunny)
        mButtonFoggy = findViewById(R.id.buttonFoggy)
    }

    override fun onStart() {
        super.onStart()

        mConditionRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val text = snapshot.value as String
                mConditionTextView.text = text
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })

        mButtonSunny.setOnClickListener {
            mConditionRef.setValue("Sunny")
        }

        mButtonFoggy.setOnClickListener {
            mConditionRef.setValue("Foggy")
        }
    }
}