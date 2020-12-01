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

class RegistrationActivity : AppCompatActivity(){

    private lateinit var email: EditText
    private lateinit var password: EditText
    private lateinit var regBtn: Button
    private lateinit var registerToLogin: TextView
    private lateinit var progressBar: ProgressBar
    private var validator = Validators()

    private var mAuth: FirebaseAuth? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registration)

        mAuth = FirebaseAuth.getInstance()

        email = findViewById(R.id.email)
        password = findViewById(R.id.password)
        regBtn = findViewById(R.id.register)
        registerToLogin = findViewById(R.id.register_to_login)
        progressBar = findViewById(R.id.progressBar)

        regBtn!!.setOnClickListener {
            registerNewUser()
        }

        var text = "Already have an account? Click here to login."

        var ss = SpannableString(text)
        var clickable = object: ClickableSpan() {
            override fun onClick(view: View) {
                var intent = Intent(applicationContext, LoginActivity::class.java)
                startActivity(intent)
            }
        }

        ss.setSpan(clickable, 31, 35, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        registerToLogin.setText(ss)
        registerToLogin.movementMethod = LinkMovementMethod.getInstance()
    }

    private fun registerNewUser() {
        // register for new user
        progressBar!!.visibility = View.VISIBLE

        val email: String = email!!.text.toString()
        val password: String = password!!.text.toString()

        if (!validator.validEmail(email)) {
            Toast.makeText(applicationContext, "Please enter a valid email!", Toast.LENGTH_LONG).show()
            return
        }

        if (!validator.validPassword(password)) {
            Toast.makeText(applicationContext, "Please enter a valid password!", Toast.LENGTH_LONG).show()
            return
        }

        mAuth!!.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(applicationContext, "Registration successful!", Toast.LENGTH_LONG).show()
                        progressBar!!.visibility = View.GONE

                        // move to login page
                        val intent = Intent(this@RegistrationActivity, LoginActivity::class.java)
                        startActivity(intent)
                    } else {
                        Toast.makeText(applicationContext, "Registration failed! Please try again later", Toast.LENGTH_LONG).show()
                        progressBar!!.visibility = View.GONE
                    }
                }
    }
}