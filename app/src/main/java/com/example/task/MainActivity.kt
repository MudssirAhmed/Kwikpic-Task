package com.example.task

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.util.Util
import com.cherryleafroad.kmagick.Magick
import com.cherryleafroad.kmagick.MagickWand
import com.example.task.utils.Permession
import com.example.task.utils.Utils
import java.io.File
import java.io.IOException


class MainActivity : AppCompatActivity() {

    // Button
    private lateinit var btnPickImae: Button

    // ImageView
    private lateinit var ivImage: ImageView

    // RequestCode
    private val GET_PHOTO = 111

    // TAG
    private val TAG = "MainActivityTAG"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnPickImae = findViewById(R.id.btn_mainActivity_pickImage)
        ivImage = findViewById(R.id.iv_mainActivity_image)

        btnPickImae.setOnClickListener {
            if(Permession.hasStoragePermession(this)) {
                val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                intent.type = "image/*"
                startActivityForResult(intent, GET_PHOTO)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(resultCode == Activity.RESULT_OK && data != null) {
            if(requestCode == GET_PHOTO) {
                val imageUri = data.data

                if (imageUri != null) {
                    try {
                        val path = Utils.getPath(this, imageUri)
                        if (path != null) {
                            showCompressImage(path)
                        } else {
                            Utils.somethingWentWrongToast(this, TAG, "In onActivityResult ERROR: Paths is null, Path: $path")
                        }
                    } catch (exception: IOException) {
                        exception.printStackTrace()
                        Utils.somethingWentWrongToast(this, TAG, "In onActivityResult::GET_PHOTO::Catch ERROR: ${exception.message}")
                    }
                }
            }
        }
    }

    private fun showCompressImage(filePath: String) {
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
            wand.imageCompressionQuality = 60
            wand.compressionQuality = 60

            wand.writeImage(path)

        }

        Log.d(TAG, "In showCompressImage Compressed-Path: $path")
        ivImage.visibility = View.VISIBLE
        Glide.with(this).load(file).into(ivImage)
    }

}