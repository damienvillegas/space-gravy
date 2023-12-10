package com.example.spacegravy

import android.content.Intent
import android.graphics.ImageDecoder
import android.graphics.drawable.AnimatedImageDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Switch
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatDelegate
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {
    private lateinit var txtUser: EditText
    private lateinit var txtPass: EditText
    private lateinit var btnSignin: Button
//    private lateinit var devBtn: Button
    private lateinit var createAccountBtn: Button
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var switch: Switch

    private val textWatcher: TextWatcher = object: TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            btnSignin.isEnabled = txtUser.text.isNotBlank() && txtPass.text.isNotBlank()
            val sharedPrefs = getSharedPreferences("savedStuff", MODE_PRIVATE)
            sharedPrefs.edit().putString("EMAIL", txtUser.text.toString()).apply()
            sharedPrefs.edit().putString("PASSWORD", txtPass.text.toString()).apply()
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            btnSignin.isEnabled = txtUser.text.isNotBlank() && txtPass.text.isNotBlank()
            val sharedPrefs = getSharedPreferences("savedStuff", MODE_PRIVATE)
            sharedPrefs.edit().putString("EMAIL", txtUser.text.toString()).apply()
            sharedPrefs.edit().putString("PASSWORD", txtPass.text.toString()).apply()
        }

        override fun afterTextChanged(s: Editable?) {
            Log.d("LoginActivity", "inside after textchanged")
        }
    }

    @RequiresApi(Build.VERSION_CODES.P)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val actionBar = supportActionBar
        actionBar!!.title = "Space Gravy"

        txtUser = findViewById(R.id.txtUsr)
        txtPass = findViewById(R.id.txtPass)
        btnSignin = findViewById(R.id.signinButton)
//        devBtn = findViewById(R.id.devBtn)
        createAccountBtn = findViewById(R.id.createAccountButton)
        switch = findViewById(R.id.signedInSwitch)
        firebaseAuth = FirebaseAuth.getInstance()

        txtUser.addTextChangedListener(textWatcher)
        txtPass.addTextChangedListener(textWatcher)

        // code help from https://www.youtube.com/watch?v=IpNLx75b0hE
        Thread {
            val source: ImageDecoder.Source = ImageDecoder.createSource(resources, R.drawable.bg)
            val drawable: Drawable = ImageDecoder.decodeDrawable(source)
            val backgroundImage: ImageView = findViewById(R.id.backgroundImage)
            backgroundImage.post {
                backgroundImage.setImageDrawable(drawable)
                (drawable as? AnimatedImageDrawable)?.start()
            }
        }.start()

        val sharedPrefs = getSharedPreferences("savedStuff", MODE_PRIVATE)
        val switchOption = sharedPrefs.getString("switch","")

        if (!switchOption.isNullOrEmpty()){
            switch.isChecked = true
            val sharedEmail = sharedPrefs.getString("EMAIL","")
            val sharedPassword = sharedPrefs.getString("PASSWORD","")

            sharedPrefs.edit().putString("switch", "1").apply()

            txtUser.setText(sharedEmail)
            txtPass.setText(sharedPassword)
        }

        switch.setOnCheckedChangeListener { _ , isChecked ->
           if (isChecked) {
               val sharedEmail = sharedPrefs.getString("EMAIL","")
               val sharedPassword = sharedPrefs.getString("PASSWORD","")

               sharedPrefs.edit().putString("switch", "1").apply()

               txtUser.setText(sharedEmail)
               txtPass.setText(sharedPassword)
           } else {
               sharedPrefs.edit().putString("switch", "").apply()
               sharedPrefs.edit().putString("SEARCH_TERM", "").apply()
               sharedPrefs.edit().putString("SEARCH_TERM", "").apply()
           }
        }

//        devBtn.setOnClickListener {
//            val intent = Intent(this@LoginActivity, MainActivity::class.java)
//            startActivity(intent)
//        }

        createAccountBtn.setOnClickListener {
            val intent = Intent(this@LoginActivity, CreateAccount::class.java)
            startActivity(intent)
        }

        // firebase auth login
        btnSignin.setOnClickListener {
            val inputtedEmail:String = txtUser.text.toString().trim()
            val inputtedPassword:String = txtPass.text.toString().trim()
            firebaseAuth.signInWithEmailAndPassword(inputtedEmail, inputtedPassword)
                .addOnCompleteListener {
                    if (it.isSuccessful){
                        val user = firebaseAuth.currentUser
                        Toast.makeText(this, "Welcome: ${user!!.email}", Toast.LENGTH_LONG).show()
                        val intent = Intent(this@LoginActivity, MainActivity::class.java)
                        intent.putExtra("email", txtUser.text.toString())
                        startActivity(intent)
                    } else {
                        val exception = it.exception
                        // Toast.makeText(this, "Sign In Failed: Please double check username and password or make account", Toast.LENGTH_LONG).show()
                        Toast.makeText(this, "Sign In Failed: $exception", Toast.LENGTH_LONG).show()
                    }
                }
        }
    }
}