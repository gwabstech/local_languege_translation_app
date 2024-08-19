package com.gwabs.englishtohausatexttranslation
import android.Manifest
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.gwabs.englishtohausatexttranslation.databinding.ActivityImageToTextBinding
import com.gwabs.englishtohausatexttranslation.network.ApiManager
import com.gwabs.englishtohausatexttranslation.network.HttpClientSingleton
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import com.techiness.progressdialoglibrary.ProgressDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.Request
import okhttp3.RequestBody
import java.io.ByteArrayOutputStream
import java.io.InputStream

class ImageToTextActivity : AppCompatActivity() {

    private val PICK_IMAGE_REQUEST = 1
    private val readStoragePermissionCode = 123 // You can use any code
    private var  extractedImageText = ""
    private var selectedImageBytes: ByteArray? = null

    private lateinit var binding: ActivityImageToTextBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityImageToTextBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.title = "Image to Text"


        binding.btnUpload.setOnClickListener {
            requestStoragePermission()
        }

        binding.buttonTranslate.setOnClickListener {
            processImage(selectedImageBytes)
        }
    }

    private fun selectImage() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    private fun handleImageSelection(data: Intent?): ByteArray? {
        if (data == null) {
            return null
        }

        try {
            val selectedImageUri = data.data
            val inputStream: InputStream? = selectedImageUri?.let {
                // Open an input stream from the selected image URI
                contentResolver.openInputStream(it)
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
        return null
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

         selectedImageBytes = handleImageSelection(data)

        if (selectedImageBytes != null) {
            Log.d("SelectedImage", "Image selected with ${selectedImageBytes!!.size} bytes")
             binding.selectedImage.text = selectedImageBytes!!.toString()


        } else {
            Toast.makeText(this,"No image selected or an error occurred",Toast.LENGTH_SHORT)
            Log.d("SelectedImage", "")
        }
    }

    private  fun processImage(imageBytes: ByteArray?) {
        val progressDialog = ProgressDialog(this)
        try {
            imageBytes?.let {
                progressDialog.show()
                lifecycleScope.launch(Dispatchers.IO) {
                    // Perform network request in a background thread
                    val extractedImageText = ApiManager.getInstance().sendImageToTextRequest(it, "English", progressDialog)
                    // Update UI on the main thread
                    launch(Dispatchers.Main) {
                        progressDialog.dismiss()
                        binding.processedText.text = extractedImageText
                        translateText(this@ImageToTextActivity,extractedImageText)
                        Log.d("TAG", "The image is $extractedImageText")
                    }
                }
            }
        } catch (e: Exception) {
            progressDialog.dismiss()
            e.printStackTrace()
        }
    }

    private fun requestStoragePermission() {
        Dexter.withContext(this)
            .withPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
            .withListener(object : PermissionListener {
                override fun onPermissionGranted(response: PermissionGrantedResponse) {
                    // Permission granted
                    selectImage()
                }

                override fun onPermissionDenied(response: PermissionDeniedResponse) {
                    Toast.makeText(this@ImageToTextActivity, "Permission denied", Toast.LENGTH_SHORT).show()
                }

                override fun onPermissionRationaleShouldBeShown(
                    request: PermissionRequest,
                    token: PermissionToken
                ) {
                    // You can show a rationale here and then continue with the permission request
                    token.continuePermissionRequest()
                }
            })
            .check()
    }


    // for translation after extracting text

    private fun translateText(context: Context,sourceText:String) {



        val progressDialog = android.app.ProgressDialog(context)
        progressDialog.setMessage("Translating...")
        progressDialog.setCancelable(false)
        progressDialog.show()

        val sourceLanguage = getLanguageCode("English")
        val targetLanguage = getLanguageCode("Hausa")

        val client = HttpClientSingleton.getInstance()

        val mediaType = "application/x-www-form-urlencoded".toMediaTypeOrNull()
        val body = RequestBody.create(mediaType, "source_language=${sourceLanguage}&target_language=${targetLanguage}&text=${sourceText}")

        // api request requirements such as auth key and base url

        val request = Request.Builder()
            .url("https://text-translator2.p.rapidapi.com/translate")
            .post(body)
            .addHeader("content-type", "application/x-www-form-urlencoded")
            .addHeader("X-RapidAPI-Key", "b8c8ee6e22mshf73df44bf8ee035p1ccf3cjsne248a0d7ba02")
            .addHeader("X-RapidAPI-Host", "text-translator2.p.rapidapi.com")
            .build()

        val gson = Gson()



        lifecycleScope.launch(Dispatchers.Main) {
            try {
                val response = withContext(Dispatchers.IO) {
                    client.newCall(request).execute()
                }

                if (response.isSuccessful) {
                    val responseBody = response.body?.string()
                    val jsonObject = gson.fromJson(responseBody, JsonObject::class.java)

                    val result = jsonObject.getAsJsonObject("data").get("translatedText").toString()
                   val  translatedText = result

                    binding.hausaTranslatedText.text = result.replace("\"", "")
                  //  binding.buttonCopy.visibility = View.VISIBLE
                } else {
                    Toast.makeText(this@ImageToTextActivity, response.body.toString(), Toast.LENGTH_SHORT).show()
                   // binding.buttonCopy.visibility = View.GONE
                }
            } catch (e: Exception) {
                Toast.makeText(this@ImageToTextActivity, e.toString(), Toast.LENGTH_SHORT).show()
               // binding.buttonCopy.visibility = View.GONE
            } finally {
                progressDialog.dismiss()
            }
        }
    }


    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}
