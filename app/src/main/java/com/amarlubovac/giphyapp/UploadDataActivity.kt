package com.amarlubovac.giphyapp

import android.Manifest
import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import kotlinx.android.synthetic.main.activity_upload_data.*
import org.jetbrains.anko.doAsync
import android.graphics.Bitmap
import android.R.attr.bitmap
import android.media.ThumbnailUtils
import android.R.attr.data
import android.net.Uri
import android.provider.DocumentsContract
import android.util.Base64
import android.util.Log
import java.io.*
import android.Manifest.permission
import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.app.ProgressDialog
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Base64.getEncoder

class UploadDataActivity : AppCompatActivity() {

    lateinit var videoFile: File

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_upload_data)

        makeRequest()

        selectBtn.setOnClickListener {
            openGallery()
        }

        uploatBtn.setOnClickListener {
            if (selectedVideoText.text.toString().length>0) {
                uploadToServer()
            }
            else {
                Toast.makeText(applicationContext, "Select video", Toast.LENGTH_SHORT).show()
            }
        }

    }

    fun openGallery() {
        val intent = Intent()
        intent.type = "video/*"
        intent.action = Intent.ACTION_GET_CONTENT
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false)
        startActivityForResult(Intent.createChooser(intent, "Choose video"), 4)
    }

    fun uploadToServer() {

        progressBar.visibility = View.VISIBLE
        val requestBody = RequestBody.create(MediaType.parse("multipart/form-data"), videoFile)
        val fileToUpload = MultipartBody.Part.createFormData("file", videoFile.getName(), requestBody)
        val apiKeyPart = RequestBody.create(MediaType.parse("multipart/form-data"), getString(R.string.api_key))
        val tagsPart = RequestBody.create(MediaType.parse("multipart/form-data"), tagsEditText.text.toString())

        val apiService = RetrofitFactory.makeApiService()
        val call: Call<Any> = apiService.uploadGiff(fileToUpload, apiKeyPart, tagsPart)

        call.enqueue(object : Callback<Any> {

            override fun onResponse(call: Call<Any>?, response: Response<Any>?) {
                progressBar.visibility = View.GONE
                if (response?.code() == 200) {
                    Toast.makeText(applicationContext, "Video is uploaded", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }

            override fun onFailure(call: Call<Any>?, t: Throwable?) {
                progressBar.visibility = View.GONE
                Toast.makeText(applicationContext, "Error", Toast.LENGTH_SHORT).show()
                finish()
            }

        })
    }

    private fun getVideoPathFromURI(uri: Uri): String
    {
        var path: String = uri.path!! // uri = any content Uri
        var videoPath = ""
        val databaseUri: Uri
        val selection: String?
        val selectionArgs: Array<String>?
        if (path.contains("/document/video:"))
        { // files selected from "Documents"
            databaseUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
            selection = "_id=?"
            selectionArgs = arrayOf(DocumentsContract.getDocumentId(uri).split(":")[1])
        }
        else
        { // files selected from all other sources, especially on Samsung devices
            databaseUri = uri
            selection = null
            selectionArgs = null
        }
        try
        {
            val projection = arrayOf(
                MediaStore.Video.Media.DATA,
                MediaStore.Video.Media._ID,
                MediaStore.Video.Media.LATITUDE,
                MediaStore.Video.Media.LONGITUDE,
                MediaStore.Video.Media.DATE_TAKEN)

            val cursor = contentResolver.query(databaseUri,
                projection, selection, selectionArgs, null)

            if (cursor?.moveToFirst()!!)
            {
                val columnIndex = cursor.getColumnIndex(projection[0])
                videoPath = cursor.getString(columnIndex)
            }
            cursor.close()
        }
        catch (e: Exception)
        {
            Log.e("TAG", e.message, e)
        }
        return videoPath
    }

    private fun makeRequest() {
        ActivityCompat.requestPermissions(this, arrayOf(READ_EXTERNAL_STORAGE), 1)
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?)
    {
        if (requestCode == 4) {
            if (data?.data !=  null) {
                val selectedVideoUri = data.data
                videoFile = File(getVideoPathFromURI(selectedVideoUri!!))
                selectedVideoText.text = "Selected video: " + videoFile.name
            }
        }
    }
}
