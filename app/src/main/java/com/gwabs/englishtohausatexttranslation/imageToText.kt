package com.gwabs.englishtohausatexttranslation

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.gwabs.englishtohausatexttranslation.databinding.ActivityImageToTextBinding
import com.gwabs.englishtohausatexttranslation.network.ApiManager
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream

class imageToText : AppCompatActivity() {

    private val PICK_IMAGE_REQUEST = 1
    private val readStoragePermissionCode = 123 // You can use any code

    val dexter = Dexter.withContext(this)
    private lateinit var binding: ActivityImageToTextBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityImageToTextBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)


        binding.btnUpload.setOnClickListener {

            requestStoragePermission(this)

        }
    }

    private fun selectImage() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    // handle image selection from files
    fun handleImageSelection(requestCode: Int, resultCode: Int, data: Intent?): ByteArray? {
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            try {
                val selectedImageUri = data.data
                val inputStream = selectedImageUri?.let {
                    // Open an input stream from the selected image URI
                    this.contentResolver.openInputStream(it)
                }

                // Convert the input stream to a byte array
                inputStream?.use {
                    val byteArrayOutputStream = ByteArrayOutputStream()
                    val buffer = ByteArray(1024)
                    var len: Int
                    while (it.read(buffer).also { len = it } > 0) {
                        byteArrayOutputStream.write(buffer, 0, len)
                    }
                    return byteArrayOutputStream.toByteArray()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return null
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        val selectedImageBytes = handleImageSelection(requestCode, resultCode, data)

        if (selectedImageBytes != null) {
            // Log the selected image as a byte array (for demonstration purposes)

            GlobalScope.launch {
                processImage(selectedImageBytes)
            }
            Log.d("SelectedImage", "Image selected with ${selectedImageBytes.size} bytes")
        } else {
            Log.d("SelectedImage", "No image selected or an error occurred")
        }

    }

   suspend fun processImage(imageByte : ByteArray?){

       try {
           val extractedImage = ApiManager.getInstance().sendImageToTextRequest(imageByte!!)

           binding.processedText.text = extractedImage
           Log.d("TAG", "The image is $extractedImage")



       }catch (e:Exception){

           e.printStackTrace()
       }

   }

    fun requestStoragePermission(activity: Activity) {
        // Initialize Dexter
        val dexter = Dexter.withContext(activity)

        // Request storage permission
        dexter.withPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
            .withListener(object : PermissionListener {

                override fun onPermissionGranted(p0: PermissionGrantedResponse?) {
                    // Permission granted
                    selectImage()
                }

                override fun onPermissionDenied(p0: PermissionDeniedResponse?) {
                    Toast.makeText(activity, "Permission denied", Toast.LENGTH_SHORT).show()

                }

                override fun onPermissionRationaleShouldBeShown(
                    p0: PermissionRequest?,
                    p1: PermissionToken?
                ) {
                    // You can show a rationale here and then continue with the permission request
                    p1?.continuePermissionRequest()
                }
            })
            .check()
    }


}