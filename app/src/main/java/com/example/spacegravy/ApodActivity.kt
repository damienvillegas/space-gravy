package com.example.spacegravy

import android.content.Intent
import android.graphics.ImageDecoder
import android.graphics.drawable.AnimatedImageDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.DatePicker
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import java.util.Calendar
import kotlin.random.Random


class ApodActivity : AppCompatActivity() {
    private lateinit var datePicker: DatePicker
    private lateinit var apodText: TextView
    private lateinit var apodRandomBtn: Button
    private lateinit var apodSubmitBtn: Button
    private lateinit var backImage: ImageView
    @RequiresApi(Build.VERSION_CODES.P)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_apod)

        val actionBar = supportActionBar
        actionBar!!.title = "Astronomy Picture of the Day"

        datePicker = findViewById(R.id.datePicker)
        apodText = findViewById(R.id.apodText)
        apodRandomBtn = findViewById(R.id.apodRandomBtn)
        apodSubmitBtn = findViewById(R.id.apodSubmitBtn)
        backImage = findViewById(R.id.apodBackImage)
        backImage.setOnClickListener {
            val intent = Intent(this@ApodActivity, MainActivity::class.java)
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

        // june 16, 1995 - earliest nasa api date
        datePicker.minDate = 803275200000

        // max date for nasa api
        datePicker.maxDate = Calendar.getInstance().timeInMillis

        apodSubmitBtn.setOnClickListener {
            val year = datePicker.year.toString()
            val month =( datePicker.month+1).toString()
            val day = datePicker.dayOfMonth.toString()
            val date = "$year-$month-$day"
            val intent = Intent(this@ApodActivity, ApodResultsActivity::class.java)
            intent.putExtra("date", date)
            startActivity(intent)
        }

        apodRandomBtn.setOnClickListener {
            while(true){
                val year = (1995..2023).random()
                val month = (1..12).random()
                var day = 1
                // get day based on month
                if (month == 1 || month == 3 || month == 5 || month == 7
                    || month == 8 || month == 10 || month == 12){
                    day = (1..31).random()
                } else if (month == 2){
                    day = (1..28).random()
                } else {
                    day = (1..30).random()
                }

                val date = "$year-$month-$day"

                // invalid api date
                if (month < 6 && day < 16 && year < 1995){
                    continue
                } else {
                    val intent = Intent(this@ApodActivity, ApodResultsActivity::class.java)
                    intent.putExtra("date", date)
                    startActivity(intent)
                    break
                }
            }
        }


    }
}