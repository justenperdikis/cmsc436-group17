package com.example.group17gonogo

import android.content.Intent
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class RegistrationActivity : AppCompatActivity(){

    private lateinit var username: EditText
    private lateinit var email: EditText
    private lateinit var password: EditText
    private lateinit var regBtn: Button
    private lateinit var registerToLogin: TextView
    private lateinit var progressBar: ProgressBar
    private var validator = Validators()

    private var mAuth: FirebaseAuth? = null

    //-----------test------------
    private val mRootRef = FirebaseDatabase.getInstance().reference
    private val mUserRef = mRootRef.child("users")
    // ------------------------

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registration)

        mAuth = FirebaseAuth.getInstance()

        username = findViewById(R.id.username)
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
        val username: String = username!!.text.toString()

        if (!validator.validUsername(username)) {
            Toast.makeText(applicationContext, "Please enter a valid username!", Toast.LENGTH_LONG).show()
            return
        }

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
                    progressBar!!.visibility = View.GONE
                    if (task.isSuccessful) {
                        Toast.makeText(applicationContext, "Registration successful!", Toast.LENGTH_LONG).show()
                        // move to login page
                        val intent = Intent(this@RegistrationActivity, LoginActivity::class.java)
                        //----------------------test ---------
                        addUser(email, username)
                        //-------------------------------------
                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(applicationContext, "Registration failed! Please try again later", Toast.LENGTH_LONG).show()
                    }
                }
    }

    // --------------------------------- test -------------------------------
    private fun addUser(email: String, username: String) {
        if (mAuth?.currentUser != null) {
            Toast.makeText(applicationContext, "User currently logged in", Toast.LENGTH_LONG).show()
            val uid = mAuth!!.uid as String
//                val id = mUserRef.child(uid).push().key
            val user = User(username, email)
            mUserRef.child(uid).setValue(user)
        }
    }
}