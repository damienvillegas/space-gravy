package com.example.spacegravy

import android.content.Intent
import android.graphics.ImageDecoder
import android.graphics.drawable.AnimatedImageDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import androidx.annotation.RequiresApi
import androidx.constraintlayout.widget.ConstraintLayout

class MainActivity : AppCompatActivity() {
    private lateinit var apodButton: Button
    private lateinit var imageLibraryButton: Button
    private lateinit var profileButton: Button
    private lateinit var constraintLayout: ConstraintLayout

    @RequiresApi(Build.VERSION_CODES.P)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        apodButton = findViewById(R.id.apodButton)
        imageLibraryButton = findViewById(R.id.imageLibraryButton)
        profileButton = findViewById(R.id.profileButton)
        constraintLayout = findViewById(R.id.constraintLayout)
        constraintLayout.setBackgroundResource(R.color.black)

//        // code help from https://www.youtube.com/watch?v=IpNLx75b0hE
//        Thread {
//            val source: ImageDecoder.Source = ImageDecoder.createSource(resources, R.drawable.bg)
//            val drawable: Drawable = ImageDecoder.decodeDrawable(source)
//            val backgroundImage: ImageView = findViewById(R.id.backgroundImage)
//            backgroundImage.post {
//                backgroundImage.setImageDrawable(drawable)
//                (drawable as? AnimatedImageDrawable)?.start()
//            }
//        }.start()

        apodButton.setOnClickListener {
            val intent = Intent(this@MainActivity, ApodActivity::class.java)
            startActivity(intent)
        }

        profileButton.setOnClickListener {
            val intent = Intent(this@MainActivity, ProfileActivity::class.java)
            startActivity(intent)
        }

       imageLibraryButton.setOnClickListener {
            val intent = Intent(this@MainActivity, NasaImageLibrary::class.java)
            startActivity(intent)
        }
    }
}