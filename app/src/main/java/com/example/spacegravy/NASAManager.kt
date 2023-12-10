package com.example.spacegravy

import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import org.json.JSONObject

class NASAManager {
    private val okHttpClient: OkHttpClient
    init{
        val builder = OkHttpClient.Builder()
        val loggingInterceptor = HttpLoggingInterceptor()
        loggingInterceptor.level = HttpLoggingInterceptor.Level.BODY
        builder.addInterceptor(loggingInterceptor)
        okHttpClient = builder.build()
    }

    fun retrieveApodDate(key: String, date: String): ApodData{
        val request = Request.Builder()
            .url("https://api.nasa.gov/planetary/apod?api_key=$key&date=$date&thumbs=true")
            .header(
                "Authorization",
                "Bearer $key"
            )
            .get()
            .build()
        try {
            val response = okHttpClient.newCall(request).execute()
            val responseBody = response.body?.string()
            var apodData = ApodData("","","","","","", "")
            if (response.isSuccessful && ! responseBody.isNullOrEmpty() ) {
                val json = JSONObject(responseBody)
                var title = ""
                var responseDate = ""
                var url = ""
                var thumbUrl = ""
                var mediaType = ""
                var explanation = ""
                var copyright = ""


                // parse json response
                title = try {
                    json.get("title").toString()
                } catch (exception: Exception){
                    ""
                }
                responseDate = try {
                    json.get("date").toString()
                } catch (exception: Exception){
                    ""
                }
                url = try {
                    json.get("url").toString()
                } catch (exception: Exception){
                    ""
                }
                mediaType = try {
                    json.get("media_type").toString()
                } catch (exception: Exception){
                    ""
                }
                explanation = try {
                    json.get("explanation").toString()
                } catch (exception: Exception){
                    ""
                }
                copyright = try {
                    json.get("copyright").toString()
                } catch (exception: Exception){
                    ""
                }

                if (mediaType == "video"){
                    thumbUrl = try {
                        json.get("thumbnail_url").toString()
                    } catch (exception: Exception){
                        ""
                    }
                }

                apodData = ApodData(
                    title = title,
                    date = responseDate,
                    url = url,
                    mediaType = mediaType,
                    explanation = explanation,
                    copyright = copyright,
                    thumbUrl = thumbUrl
                )

            }
            return apodData
        } catch (exception: Exception){
            Log.e("NASA Manager", "retrieveApodDate error with okHTTPClient Request: $exception")
        }
        return ApodData("","","","","","","")
    }

    fun retrieveImages(query: String): List<LibraryData> {
        val request = Request.Builder()
            .url("https://images-api.nasa.gov/search?q=$query&media_type=image&page_size=5")
            .get()
            .build()
        try {
            val response = okHttpClient.newCall(request).execute()
            val responseBody = response.body?.string()
            val libraryData = mutableListOf<LibraryData>()
            if (response.isSuccessful && !responseBody.isNullOrEmpty()) {
                val json = JSONObject(responseBody)
                val images = json.getJSONObject("collection").getJSONArray("items")

                for (i in 0 until images.length()) {
                    val currentImage = images.getJSONObject(i)
                    val currentData = currentImage.getJSONArray("data").getJSONObject(0)
                    val currentLinks = currentImage.getJSONArray("links").getJSONObject(0)
                    val nasaID = currentData.getString("nasa_id")
                    val title = currentData.getString("title")
                    val date = currentData.getString("date_created")
                    val url = currentLinks.getString("href")
                    val description = currentData.getString("description")
                    val data = LibraryData(
                        nasaID = nasaID,
                        title = title,
                        date = date,
                        url = url,
                        description = description
                    )
                    libraryData.add(data)
                }

            }
            return libraryData
        } catch (exception: Exception) {
            Log.e("NASA Manager", "retrieveImages error with okHTTPClient Request: $exception")
        }
        return listOf()
    }


    fun retrieveImagesFromProfile(uid: String, reference: DatabaseReference): List<LibraryData> {
        val libraryData = mutableListOf<LibraryData>()
        reference.child(uid).child("images").addListenerForSingleValueEvent(object: ValueEventListener{
            override fun onDataChange(dataSnapshot: DataSnapshot) {

                for (item in dataSnapshot.children){
                    val data = LibraryData(
                        nasaID = item.child("nasaID").value.toString(),
                        title = item.child("title").value.toString(),
                        date = item.child("date").value.toString(),
                        url = item.child("url").value.toString(),
                        description = item.child("description").value.toString(),
                    )
                    libraryData.add(data)
                    Log.d("NASA Manager item", item.toString())
                    Log.d("NASA Manager data", data.toString())
                    Log.d("NASA Manager libraryData", libraryData.toString())
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d("NASA Manager", "retrieveImagesFromProfile error $error")
            }
        })
        return libraryData
    }
}