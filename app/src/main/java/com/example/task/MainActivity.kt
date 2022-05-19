package com.example.task

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.cherryleafroad.kmagick.Magick
import com.cherryleafroad.kmagick.MagickWand
import com.example.task.utils.Permession
import com.example.task.utils.Utils
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException


class MainActivity : AppCompatActivity() {

    // Button
    private lateinit var btnPickImage: Button
    private lateinit var btnCompressImage: Button

    // ImageView
    private lateinit var ivImage: ImageView

    // RequestCode
    private val GET_PHOTO = 111

    // DATA
    private var selectedImageUri: Uri? = null

    // TAG
    private val TAG = "MainActivityTAG"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        ivImage = findViewById(R.id.iv_mainActivity_image)
        btnPickImage = findViewById(R.id.btn_mainActivity_pickImage)
        btnCompressImage = findViewById(R.id.btn_mainActivity_compressImage)

        btnPickImage.setOnClickListener {
            if(Permession.hasStoragePermession(this)) {
                val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                intent.type = "image/*"
                startActivityForResult(intent, GET_PHOTO)
            }
        }
        btnCompressImage.setOnClickListener {
            if(selectedImageUri != null) {
                val progressDialog = Utils.ProgressDialog(this, "Compressing...")

                GlobalScope.launch {
                    try {
                        val path = Utils.getPath(this@MainActivity, selectedImageUri!!)
                        if (path != null) {
                            showCompressImage(path, progressDialog)
                        } else {
                            Utils.dismissDialog(this@MainActivity, progressDialog)
                            Utils.somethingWentWrongToast(this@MainActivity, TAG, "In onActivityResult ERROR: Paths is null, Path: $path")
                        }
                    } catch (exception: IOException) {
                        Utils.dismissDialog(this@MainActivity, progressDialog)
                        exception.printStackTrace()
                        Utils.somethingWentWrongToast(this@MainActivity, TAG, "In onActivityResult::GET_PHOTO::Catch ERROR: ${exception.message}")
                    }
                }

            } else {
                Toast.makeText(this, "Please select image first", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(resultCode == Activity.RESULT_OK && data != null) {
            if(requestCode == GET_PHOTO) {
                selectedImageUri = data.data

                if (selectedImageUri != null) {
                    Log.d(TAG, "In showCompressImage ImageUri: $selectedImageUri")
                    Glide.with(this).load(selectedImageUri).into(ivImage)
                }
            }
        }
    }

    private fun showCompressImage(filePath: String, progressDialog: ProgressDialog) {
        Log.d(TAG, "In showCompressImage FilePath: $filePath")

        val downloadDir = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS + "/Compressed")
        if (!downloadDir!!.exists()) {
            downloadDir.mkdir()
        }

        val name = System.currentTimeMillis().toString()
        val path = "$downloadDir/$name.png"
        val file = File(path)

        Magick.initialize().use {
            val wand = MagickWand()

            wand.readImage(filePath)

//            wand.setSamplingFactors(doubleArrayOf(2.0, 1.0, 1.0))
//            wand.adaptiveResizeImage(500, 500)
            wand.imageCompressionQuality = 6
//            wand.compressionQuality = 6

            wand.writeImage(path)
        }

        Log.d(TAG, "In showCompressImage Compressed-Path: $path")

        runOnUiThread {
            Glide.with(this).load(file).into(ivImage)
            Utils.dismissDialog(this ,progressDialog)
        }

    }

}