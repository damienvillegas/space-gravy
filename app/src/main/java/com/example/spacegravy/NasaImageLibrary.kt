package com.example.spacegravy

import android.content.Intent
import android.graphics.ImageDecoder
import android.graphics.drawable.AnimatedImageDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.annotation.RequiresApi
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class NasaImageLibrary : AppCompatActivity() {
    private lateinit var libSearchTerm: EditText
    private lateinit var recyclerView: RecyclerView
    private lateinit var submitButton: Button
    private lateinit var backImage: ImageView
    private lateinit var progressBar: ProgressBar

    @RequiresApi(Build.VERSION_CODES.P)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nasa_image_library)

        val actionBar = supportActionBar
        actionBar!!.title = "Nasa Image and Video Library"

        libSearchTerm = findViewById(R.id.libSearchTerm)
        recyclerView = findViewById(R.id.imageRecyclerView)
        submitButton = findViewById(R.id.libSubmitButton)
        progressBar = findViewById(R.id.libProgressBar)
        backImage = findViewById(R.id.libBackImage)
        backImage.setOnClickListener {
            val intent = Intent(this@NasaImageLibrary, MainActivity::class.java)
            startActivity(intent)
        }

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

        val nasaManager = NASAManager()
        var libraryData = listOf<LibraryData>()

        submitButton.setOnClickListener {
            CoroutineScope(Dispatchers.IO).launch {
                libraryData = nasaManager.retrieveImages(libSearchTerm.text.toString())
                withContext(Dispatchers.Main) {
                    val adapter = ImageAdapter(libraryData)
                    recyclerView.adapter = adapter
                    progressBar.isVisible = false
                    adapter.setOnItemClickListener(object : ImageAdapter.onItemClickListener {
                        override fun onItemClick(position: Int) {
                            val browserIntent = Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse(libraryData[position].url)
                            )
                            startActivity(browserIntent)
                        }
                    })
                    adapter.activityInfo(contentResolver, this@NasaImageLibrary)
                    recyclerView.layoutManager = LinearLayoutManager(this@NasaImageLibrary)
                }
            }
        }
        // first time loading
        CoroutineScope(Dispatchers.IO).launch {
            libraryData = nasaManager.retrieveImages(libSearchTerm.text.toString())
            withContext(Dispatchers.Main) {
                val adapter = ImageAdapter(libraryData)
                recyclerView.adapter = adapter
                progressBar.isVisible = false
                adapter.setOnItemClickListener(object : ImageAdapter.onItemClickListener {
                    override fun onItemClick(position: Int) {
                        val browserIntent = Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse(libraryData[position].url)
                        )
                        startActivity(browserIntent)
                    }
                })
                adapter.activityInfo(contentResolver, this@NasaImageLibrary)
                recyclerView.layoutManager = LinearLayoutManager(this@NasaImageLibrary)
            }
        }
    }
}