package com.example.group17_gonogo

import android.content.Intent
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {

    private var mAuth: FirebaseAuth? = null

    private lateinit var userEmail: EditText
    private lateinit var userPassword: EditText
    private lateinit var loginToRegister: TextView
    private lateinit var loginBtn: Button
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        mAuth = FirebaseAuth.getInstance()

        userEmail = findViewById(R.id.email)
        userPassword = findViewById(R.id.password)
        loginToRegister = findViewById(R.id.login_to_register)
        loginBtn = findViewById(R.id.login)
        progressBar = findViewById(R.id.progressBar)

        loginBtn!!.setOnClickListener {
            loginUserAccount()
        }

        var text = "Don\'t have an account? Click here to register."

        var ss = SpannableString(text)
        var clickable = object: ClickableSpan() {
            override fun onClick(view: View) {
                var intent = Intent(applicationContext, RegistrationActivity::class.java)
                startActivity(intent)
            }
        }

        ss.setSpan(clickable, 29, 33, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        loginToRegister.setText(ss)
        loginToRegister.movementMethod = LinkMovementMethod.getInstance()
    }

    private fun loginUserAccount() {
        var email = userEmail?.text.toString()
        var pass = userPassword?.text.toString()

        if (email == null) {
            Toast.makeText(applicationContext, "Please enter an email!", Toast.LENGTH_LONG).show()
            return
        }

        if (pass == null) {
            Toast.makeText(applicationContext, "Please enter a password!", Toast.LENGTH_LONG).show()
            return
        }

        mAuth!!.signInWithEmailAndPassword(email, pass).addOnCompleteListener() {
            if (it.isSuccessful) {
               Toast.makeText(applicationContext, "Login successful!", Toast.LENGTH_LONG).show()
                var intent = Intent(this, MainActivity::class.java)
                // intent.putExtra(USER_ID, mAuth!!.currentUser?.uid)
                startActivity(intent)
            } else {
                Toast.makeText(applicationContext,"Login failed! Please try again later.", Toast.LENGTH_LONG).show()
            }
        }
    }
}