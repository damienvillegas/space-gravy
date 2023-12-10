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
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class CreateAccount : AppCompatActivity() {
    private lateinit var firstname: EditText
    private lateinit var lastname: EditText
    private lateinit var email: EditText
    private lateinit var password: EditText
    private lateinit var confirmPassword: EditText
    private lateinit var createAccount: Button
    private lateinit var backImage: ImageView
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseDatabase: FirebaseDatabase

    private val textWatcher: TextWatcher = object: TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
//            createAccount.isEnabled = email.text.isNotBlank() && password.text.isNotBlank() && password.text.equals(confirmPassword.text)
            createAccount.isEnabled = email.text.isNotBlank()
                    && password.text.isNotBlank()
                    && password.text.toString().trim() == confirmPassword.text.toString().trim()
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
//            createAccount.isEnabled = email.text.isNotBlank() && password.text.isNotBlank() && password.text.equals(confirmPassword.text)
            createAccount.isEnabled = email.text.isNotBlank()
                    && password.text.isNotBlank()
                    && password.text.toString().trim() == confirmPassword.text.toString().trim()
        }

        override fun afterTextChanged(s: Editable?) {
            Log.d("LoginActivity", "inside after textchanged")
        }
    }
    @RequiresApi(Build.VERSION_CODES.P)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_account)

        val actionBar = supportActionBar
        actionBar!!.title = "Create Account"

        firstname = findViewById(R.id.createFirstname)
        lastname = findViewById(R.id.createLastname)
        email = findViewById(R.id.createEmail)
        password = findViewById(R.id.createPassword)
        confirmPassword = findViewById(R.id.createConfirmPassword)
        createAccount = findViewById(R.id.createCreateAccountButton)
        backImage = findViewById(R.id.backImage)
        firebaseDatabase = FirebaseDatabase.getInstance()

        firebaseAuth = FirebaseAuth.getInstance()

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

        backImage.setOnClickListener {
            val intent = Intent(this@CreateAccount, LoginActivity::class.java)
            startActivity(intent)
        }

        email.addTextChangedListener(textWatcher)
        password.addTextChangedListener(textWatcher)
        confirmPassword.addTextChangedListener(textWatcher)

        createAccount.setOnClickListener {
            val inputtedEmail:String = email.text.toString().trim()
            val inputtedPassword:String = password.text.toString().trim()
            firebaseAuth.createUserWithEmailAndPassword(inputtedEmail, inputtedPassword)
                .addOnCompleteListener {
                    if (it.isSuccessful){
                        val user = firebaseAuth.currentUser
                        // Add profile to database
                        val fname = firstname.text.toString()
                        val lname = lastname.text.toString()
                        val reference = firebaseDatabase.getReference("Profile")
                        val uid = firebaseAuth.currentUser?.uid.toString()
                        val profile = GravyProfile (
                            uid = uid,
                            email = inputtedEmail,
                            firstname = fname,
                            lastname = lname,
                            profileImage = "",
                            bio = "",
                        )
                        reference.child(uid).setValue(profile)
                        Toast.makeText(this, "Account Created: ${user!!.email}", Toast.LENGTH_LONG).show()
                        val intent = Intent(this@CreateAccount, MainActivity::class.java)
                        intent.putExtra("email", email.text.toString())
                        startActivity(intent)
                    } else {
                        val exception = it.exception
                        // Toast.makeText(this, "Sign In Failed: Please double check username and password or make account", Toast.LENGTH_LONG).show()
                        Toast.makeText(this, "Failed to Create Account: $exception", Toast.LENGTH_LONG).show()
                        Log.d("CREATE ACCOUNT", "$exception")
                    }
                }
        }

    }
}