package com.example.group17gonogo

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.drawable.BitmapDrawable
import android.media.AudioManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    private lateinit var logo: ImageView
    private lateinit var startButton: Button
    private lateinit var instructionButton: Button
    private lateinit var leaderboardButton: Button
    private lateinit var loginLogoutButton: Button
    private lateinit var exitButton: Button

    private lateinit var mAudioManager: AudioManager
    private lateinit var mDialog: AlertDialog
    var isRedTheme: Boolean = false

    private var mAuth: FirebaseAuth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //get audio manager
        mAudioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager

        logo = findViewById(R.id.logoView)
        startButton = findViewById(R.id.goNoGoStart_button)
        instructionButton = findViewById(R.id.reactionTest_button)
        leaderboardButton = findViewById(R.id.main_leaderboard_button)
        loginLogoutButton = findViewById(R.id.main_login_or_logout_button)
        exitButton = findViewById(R.id.exit_button)

        supportActionBar!!.setTitle("")
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.main_option_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        //Changes themes to red or green
        if (item.itemId == R.id.change_theme) {
            mAudioManager.playSoundEffect(AudioManager.FX_KEYPRESS_SPACEBAR)

            Log.i(TAG, theme.resources.toString())
            if (!isRedTheme){
                setTheme(R.style.Theme_Red)
                isRedTheme = true
            }
            else {
                setTheme(R.style.Theme_Group17GoNoGo)
                isRedTheme = false
            }
            setContentView(R.layout.activity_main)
        }

        //Display alert dialog of extra information
        if (item.itemId == R.id.more_info){
            mAudioManager.playSoundEffect(AudioManager.FX_KEYPRESS_SPACEBAR)

            mDialog = AlertDialog.Builder(this)
                    .setTitle("More Information")
                    .setMessage("This app is brought to you by Give Us An A Inc." +
                            " With members Giovanni, George and Justen")
                    .setCancelable(true)
                    .setPositiveButton(
                            "Ok"
                    ) { _, _ ->
                        (this as MainActivity)
                    }.create()
            mDialog.show()
        }

        return true
    }

    //Start the Go-No-Go test
    fun startGoNoGo(view: View) {
//        Log.i(TAG, "Entered startTest()")
        mAudioManager.playSoundEffect(AudioManager.FX_KEYPRESS_SPACEBAR)

        val mGoNoGoIntent = Intent(
            this@MainActivity,
            GoNoGoActivity::class.java)

        startActivity(mGoNoGoIntent)
    }

    //start the simple reaction test
    fun startReactionTest(view: View) {
//        Log.i(TAG, "Entered startReactionTest()")
        mAudioManager.playSoundEffect(AudioManager.FX_KEYPRESS_SPACEBAR)
        //Show content of instructions
        val mReactionTestIntent = Intent(
            this@MainActivity,
            ReactionActivity::class.java)

        startActivity(mReactionTestIntent)
    }

    //shows the current leaderboard of all users
    fun showLeaderboard(view: View) {
        var intent = Intent(applicationContext, LeaderboardActivity::class.java)
        startActivity(intent)
    }

    fun leaderboardOptions() {
        // not yet completed
        var dialog = AlertDialog.Builder(this).create()

        dialog.setTitle("Choose Leaderboard")

        dialog.setCancelable(true)
        dialog.create()
        dialog.show()
    }

    //Logs out the current user and displays toast
    fun loginLogout(view: View) {
        mAudioManager.playSoundEffect(AudioManager.FX_KEYPRESS_SPACEBAR)
        if (mAuth.currentUser != null) {        // logout
            mAuth.signOut()
            loginLogoutButton.text = "Login"
            Toast.makeText(applicationContext, "Current user logged out.", Toast.LENGTH_LONG).show()
        } else {                                // login
            //Toast.makeText(applicationContext, "No user currently logged in.", Toast.LENGTH_LONG).show()
            var intent = Intent(applicationContext, LoginActivity::class.java)
            startActivity(intent)
        }
    }

    //Exits the app
    fun exitApp(view: View) {
        super.onBackPressed()
//        Log.i(TAG, "Exiting app")
    }

    override fun onResume() {
        super.onResume()
        if (mAuth.currentUser == null) {
            // Log.i(TAG, "onResume called with no user")
            loginLogoutButton.text = "Login"
        } else {
            // Log.i(TAG, "onResume called with user ${mAuth.currentUser!!.uid}")
            loginLogoutButton.text = "Logout"
        }

    }

    companion object {
        private const val TAG = "GoNoGo"
    }
}