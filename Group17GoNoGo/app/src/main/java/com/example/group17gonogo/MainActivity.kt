package com.example.group17gonogo

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    private lateinit var startButton: Button
    private lateinit var instructionButton: Button
    private lateinit var exitButton: Button

    private var mAuth: FirebaseAuth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        startButton = findViewById(R.id.start_button)
        instructionButton = findViewById(R.id.instruction_button)
        exitButton = findViewById(R.id.exit_button)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.main_option_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.login) {
            // move to login page
            var intent = Intent(applicationContext, LoginActivity::class.java)
            startActivity(intent)
        }

        if (item.itemId == R.id.register) {
            // move to register page
            var intent = Intent(applicationContext, RegistrationActivity::class.java)
            startActivity(intent)
        }

        if (item.itemId == R.id.logout) {
            if (mAuth.currentUser != null) {
                mAuth.signOut()
                Toast.makeText(applicationContext, "Current user logged out.", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(applicationContext, "No user currently logged in.", Toast.LENGTH_LONG).show()
            }

        }

        return true
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
        val mInstructionIntent = Intent(
            this@MainActivity,
            InstructionsActivity::class.java)

        startActivity(mInstructionIntent)
    }
    fun exitApp(view: View) {
        super.onBackPressed()
//        Log.i(TAG, "Exiting app")
    }

    companion object {
        private const val TAG = "GoNoGo"
    }
}