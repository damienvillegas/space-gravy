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
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ProfileActivity : AppCompatActivity() {
    private lateinit var profileNameText: TextView
    private lateinit var profileEmail: TextView
    private lateinit var recyclerView: RecyclerView
    private lateinit var backImage: ImageView
    private lateinit var progressBar: ProgressBar
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseDatabase: FirebaseDatabase

    @RequiresApi(Build.VERSION_CODES.P)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        profileNameText = findViewById(R.id.profileNameText)
        profileEmail = findViewById(R.id.profileEmailText)
        recyclerView = findViewById(R.id.profileRecyclerView)
        progressBar = findViewById(R.id.profileProgressBar)
        backImage = findViewById(R.id.profileBackImage)
        backImage.setOnClickListener {
            val intent = Intent(this@ProfileActivity, MainActivity::class.java)
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

        firebaseAuth = FirebaseAuth.getInstance()
        firebaseDatabase = FirebaseDatabase.getInstance()

        // read from DB
        val reference = firebaseDatabase.getReference("Profile")
        val uid = firebaseAuth.currentUser?.uid.toString()
        var name = ""
        reference.child(uid).child("firstname").get().addOnSuccessListener {
            name = it.value.toString()
            profileNameText.text = name

        }
        reference.child(uid).child("lastname").get().addOnSuccessListener {
           name += " "+it.value.toString()
            profileNameText.text = name
        }
        reference.child(uid).child("email").get().addOnSuccessListener {
            profileEmail.text = it.value.toString()
        }


        reference.child(uid).child("images").addListenerForSingleValueEvent(object:
            ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val nasaManager = NASAManager()
                var libraryData = mutableListOf<LibraryData>()
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
                }
                CoroutineScope(Dispatchers.IO).launch {
                    Log.d("PROFILE ACTIVITY", libraryData.toString())
                    withContext(Dispatchers.Main) {
                        val adapter = ProfileAdapter(libraryData)
                        recyclerView.adapter = adapter
                        progressBar.isVisible = false
                        adapter.setOnItemClickListener(object : ProfileAdapter.onItemClickListener {
                            override fun onItemClick(position: Int) {
                                val browserIntent = Intent(
                                    Intent.ACTION_VIEW,
                                    Uri.parse(libraryData[position].url)
                                )
                                startActivity(browserIntent)
                            }
                        })
                        adapter.activityInfo(contentResolver, this@ProfileActivity)
                        recyclerView.layoutManager = LinearLayoutManager(this@ProfileActivity)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d("NASA Manager", "retrieveImagesFromProfile error $error")
            }
        })


//        CoroutineScope(Dispatchers.IO).launch {
//            libraryData = nasaManager.retrieveImagesFromProfile(uid, reference)
//            Log.d("PROFILE ACTIVITY", libraryData.toString())
//            withContext(Dispatchers.Main) {
//                val adapter = ImageAdapter(libraryData)
//                recyclerView.adapter = adapter
//                adapter.setOnItemClickListener(object : ImageAdapter.onItemClickListener {
//                    override fun onItemClick(position: Int) {
//                        val browserIntent = Intent(
//                            Intent.ACTION_VIEW,
//                            Uri.parse(libraryData[position].url)
//                        )
//                        startActivity(browserIntent)
//                    }
//                })
//                recyclerView.layoutManager = LinearLayoutManager(this@ProfileActivity)
//            }
//        }

    }
}