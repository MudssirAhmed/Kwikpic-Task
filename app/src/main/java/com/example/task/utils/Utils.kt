package com.example.task.utils

import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import android.text.TextUtils
import android.util.Log
import android.widget.Toast
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

object Utils {
    fun compressImageAndSaveInCache(context: Context, bitmap: Bitmap, listener: (Boolean, String, String) -> Unit) {
        val cachePath: File
        var fileName = "img"
        val childDir = "images"
        val fileExtension = ".jpg"
        val compressQuality = 70
        val name = System.currentTimeMillis().toString()

        if (!TextUtils.isEmpty(name)) {
            fileName = name
        }
        try {
            cachePath = File(context.getCacheDir(), childDir)
            if (!cachePath.exists()) {
                cachePath.mkdir()
            }
            val stream = FileOutputStream("$cachePath/$fileName$fileExtension")
            bitmap.compress(Bitmap.CompressFormat.PNG, compressQuality, stream)
            stream.close()
            val path = cachePath.path + "/" + fileName + fileExtension

            listener(true, path, "")

        } catch (e: IOException) {
            listener(false, "", e.message.toString())
        }
    }

    fun getPath(context: Context, uri: Uri): String? {
        val projection = arrayOf(MediaStore.Video.Media.DATA)
        val cursor = context.contentResolver.query(uri, projection, null, null, null)
        return if (cursor != null) {
            val columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA)
            cursor.moveToFirst()
            cursor.getString(columnIndex)
        } else null
    }

    fun somethingWentWrongToast(context: Context?, TAG: String?, error: String?) {
        Toast.makeText(context, "Something went wrong please try again", Toast.LENGTH_SHORT).show()
        Log.e(TAG, error!!)
    }

    fun ProgressDialog(context: Context, text: String): ProgressDialog {
        val progressDialog = ProgressDialog(context)
        progressDialog.setMessage(text)
        progressDialog.setCancelable(false)
        progressDialog.show()
        return progressDialog
    }

    fun dismissDialog(activity: Activity, pd: ProgressDialog?) {
        activity.runOnUiThread {
            if (pd != null) {
                if (pd.isShowing) {
                    pd.dismiss()
                }
            }
        }
    }
}