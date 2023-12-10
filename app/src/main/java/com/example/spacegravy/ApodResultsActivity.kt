package com.example.spacegravy

import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.ImageDecoder
import android.graphics.drawable.AnimatedImageDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.PackageManagerCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.common.collect.BiMap
import com.squareup.picasso.Picasso
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

class ApodResultsActivity : AppCompatActivity() {
    private lateinit var apodImage: ImageView
    private lateinit var explanationText: TextView
    private lateinit var titleText: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var backImage: ImageView
    private lateinit var download: Button

    @RequiresApi(Build.VERSION_CODES.P)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_apod_results)

        val actionBar = supportActionBar
        actionBar!!.title = "Astronomy Picture of the Day"

        val date = intent.getStringExtra("date").toString()

        apodImage = findViewById(R.id.apodImage)
        explanationText = findViewById(R.id.explanationText)
        titleText = findViewById(R.id.titleText)
        progressBar = findViewById(R.id.apodProgressBar)
        backImage = findViewById(R.id.apodResultsBackImage)
        download = findViewById(R.id.apodDownloadButton)
        backImage.setOnClickListener {
            val intent = Intent(this@ApodResultsActivity, ApodActivity::class.java)
            startActivity(intent)
        }

        // code help from https://www.youtube.com/watch?v=IpNLx75b0hE
        Thread {
            val source: ImageDecoder.Source = ImageDecoder.createSource(resources, R.drawable.bg)
            val drawable: Drawable = ImageDecoder.decodeDrawable(source)
            val backgroundImage: ImageView = findViewById(R.id.apodBackgroundImage)
            backgroundImage.post {
                backgroundImage.setImageDrawable(drawable)
                (drawable as? AnimatedImageDrawable)?.start()
            }
        }.start()

        var apodData: ApodData
        val nasaManager = NASAManager()
        val apiKey = getString(R.string.nasa_api_key)

        // accounts for network errors
        CoroutineScope(Dispatchers.IO).launch{
            apodData = nasaManager.retrieveApodDate(apiKey, date)
            withContext(Dispatchers.Main){
                Log.d("PICASSO", apodData.url)
                Picasso.get().setIndicatorsEnabled(true)
                if (apodData.mediaType == "image"){
                    Picasso.get().load(apodData.url).into(apodImage)
                } else {
                    Picasso.get().load(apodData.thumbUrl).into(apodImage)
                }
                explanationText.text = apodData.explanation + "\n\n" + apodData.copyright + " " + apodData.date
                explanationText.movementMethod = ScrollingMovementMethod()
                titleText.text = apodData.title
                progressBar.isVisible = false
                apodImage.setOnClickListener {
                    val intent = Intent(Intent.ACTION_VIEW,  Uri.parse(apodData.url))
                    startActivity(intent)
                }
                download.setOnClickListener {
                    useStorage(apodImage)
                }
            }
        }

    }

    private fun useStorage(image: ImageView){
        // code help from https://www.youtube.com/watch?v=AuID5KSYXgQ
        Log.d("STORAGE", "useStorage()")
        var bitmap: Bitmap? = null
        try {
            bitmap = Bitmap.createBitmap(image.measuredWidth, image.measuredHeight, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            image.draw(canvas)
        } catch (e: Exception){
            Log.d("STORAGE", "error $e")
        }
        val imageName = "spacegravy_${System.currentTimeMillis()}.jpg"
        var fos: OutputStream? = null
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
            this.contentResolver?.also {resolver ->
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, imageName)
                    put(MediaStore.MediaColumns.MIME_TYPE, "image/jpg")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)

                }
                val imageUri: Uri? = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                fos = imageUri?.let {
                    resolver.openOutputStream(it)
                }
            }
        } else {
            val imagesDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
            val currImage = File(imagesDirectory, imageName)
            fos = FileOutputStream(currImage)
        }
        if (bitmap != null) {
            fos?.use {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it)
                Toast.makeText(this, "Image Saved to Photo Gallery", Toast.LENGTH_LONG).show()
            }
        }
    }
}