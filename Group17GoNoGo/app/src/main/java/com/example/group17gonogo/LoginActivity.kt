package com.example.group17gonogo

import android.content.Intent
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.TextUtils
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

        loginBtn.setOnClickListener {
            loginUserAccount()
        }

        val text = "Don\'t have an account? Click here to register."

        val ss = SpannableString(text)
        val clickable = object: ClickableSpan() {
            override fun onClick(view: View) {
                val intent = Intent(applicationContext, RegistrationActivity::class.java)
                startActivity(intent)
            }
        }

        ss.setSpan(clickable, 29, 33, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        loginToRegister.setText(ss)
        loginToRegister.movementMethod = LinkMovementMethod.getInstance()

        // hide the action bar
        supportActionBar!!.hide()
    }

    private fun loginUserAccount() {
        progressBar.visibility = View.VISIBLE

        val email: String = userEmail.text.toString()
        val password: String = userPassword.text.toString()

        if (TextUtils.isEmpty(email)) {
            Toast.makeText(applicationContext, "Please enter an email!", Toast.LENGTH_LONG).show()
            return
        }

        if (TextUtils.isEmpty(password)) {
            Toast.makeText(applicationContext, "Please enter a password!", Toast.LENGTH_LONG).show()
            return
        }

        mAuth!!.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener {
                progressBar.visibility = View.GONE
                if (it.isSuccessful) {
                   Toast.makeText(applicationContext, "Login successful!", Toast.LENGTH_LONG)
                       .show()
                    val mainActivityIntent = Intent(this@LoginActivity, MainActivity::class.java)
                    startActivity(mainActivityIntent)
                    finish()
                } else {
                    Toast.makeText(
                        applicationContext,
                        "Login failed! Please try again later.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
    }
}