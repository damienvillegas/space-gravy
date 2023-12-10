package com.example.spacegravy

import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.squareup.picasso.Picasso
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.PackageManagerCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.common.collect.BiMap


class ProfileAdapter (private val data: List<LibraryData>): RecyclerView.Adapter<ProfileAdapter.ViewHolder>() {
    private lateinit var mListener: onItemClickListener
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseDatabase: FirebaseDatabase
    private lateinit var resolver: ContentResolver
    private lateinit var context: Context
    interface onItemClickListener {
        fun onItemClick(position: Int)
    }

    fun setOnItemClickListener(listener: onItemClickListener){
        mListener = listener
    }

    class ViewHolder (rootLayout: View, listener: onItemClickListener): RecyclerView.ViewHolder(rootLayout){
        val image: ImageView = rootLayout.findViewById(R.id.cardImageView)
        val title: TextView = rootLayout.findViewById(R.id.libTitle)
        val save: CheckBox = rootLayout.findViewById(R.id.libCheckBox)
        val cardView: CardView = rootLayout.findViewById(R.id.cardView)
        var download: Button = rootLayout.findViewById(R.id.libDownloadButton)

        init {
            rootLayout.setOnClickListener {
                listener.onItemClick(adapterPosition)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        Log.d("RV", "Inside onCreateViewHolder")
        val layoutInflater: LayoutInflater = LayoutInflater.from(parent.context)
        val rootLayout: View = layoutInflater.inflate(R.layout.cardviewlayout2, parent, false)
        val viewHolder = ViewHolder(rootLayout, mListener)


        return viewHolder
    }

    override fun getItemCount(): Int {
        Log.d("RV", "Inside getItemCount()")
        return data.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val  currentData = data[position]
        if (currentData.url.isNotBlank()){
            Picasso.get().setIndicatorsEnabled(true)
            Picasso.get().load(currentData.url).into(holder.image)
            val articleListeningContext = holder.cardView.context
            holder.title.text = currentData.title
            holder.cardView.setOnClickListener{
                val intent = Intent(Intent.ACTION_VIEW)
                intent.data = Uri.parse(currentData.url)
                articleListeningContext.startActivity(intent)
            }
            holder.save.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked){
                    firebaseDatabase = FirebaseDatabase.getInstance()
                    firebaseAuth = FirebaseAuth.getInstance()
                    val uid = firebaseAuth.currentUser?.uid.toString()
                    val reference = firebaseDatabase.getReference("Profile")
                    reference.child(uid).child("images").child(currentData.nasaID).setValue(currentData)
                } else {
                    firebaseDatabase = FirebaseDatabase.getInstance()
                    firebaseAuth = FirebaseAuth.getInstance()
                    val uid = firebaseAuth.currentUser?.uid.toString()
                    val reference = firebaseDatabase.getReference("Profile")
                    reference.child(uid).child("images").child(currentData.nasaID).removeValue()
                }
            }
            holder.download.setOnClickListener {
                useStorage(holder.image)
            }
        }

        Log.d("RV", "inside onBindViewHolder at position $position")
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
            resolver.also {resolver ->
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
                Toast.makeText(context, "Image Saved to Photo Gallery", Toast.LENGTH_LONG).show()
            }
        }
    }

    fun activityInfo(curResolver: ContentResolver, curContext: Context){
        resolver = curResolver
        context = curContext
    }
}