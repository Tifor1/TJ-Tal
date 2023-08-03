package translator.tifor

import android.Manifest
import android.app.Activity
import android.app.ProgressDialog
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.Menu
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizer
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import translator.tifor.databinding.ActivityImTeBinding
import kotlin.Exception

@Suppress("UNREACHABLE_CODE", "DEPRECATED_IDENTITY_EQUALS")
class ImTeActivity : AppCompatActivity() {

    private companion object {
        private const val CAMERA_REQUEST_CODE = 100
        private const val STORAGE_REQUEST_CODE = 101
    }

    private lateinit var binding: ActivityImTeBinding
    private lateinit var imageUri: Uri
    private lateinit var cameraPermission: Array<String>
    private lateinit var storagePermission: Array<String>
    private lateinit var progressDialog: ProgressDialog
    private lateinit var textRecognizer: TextRecognizer

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityImTeBinding.inflate(layoutInflater)
        setContentView(binding.root)


        cameraPermission =
            arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        storagePermission = arrayOf(Manifest.permission.READ_MEDIA_IMAGES)


        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Please wait...")
        progressDialog.setCanceledOnTouchOutside(false)

        textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)


        binding.btnCamera.setOnClickListener {

            showInputImageDialog()

        }

        binding.btnImage.setOnClickListener {

            try {
                if (imageUri == null) {

                    showInputImageDialog()

                    showToast("Pick Image First...")

                } else {

                    recognizeTextFromImage()

                }
            } catch (e: Exception) {

                showToast(e.message!!)

            }

        }


    }

    private fun recognizeTextFromImage() {

        progressDialog.setMessage("Preparing Image...")
        progressDialog.show()

        try {

            val inputImage = InputImage.fromFilePath(this, imageUri)

            progressDialog.setMessage("Recognizing Text...")

            val textTaskResult = textRecognizer.process(inputImage).addOnSuccessListener {

                progressDialog.dismiss()

                val recognizedText = it.text

                binding.textView.setText(recognizedText)

            }.addOnFailureListener {

                progressDialog.dismiss()
                showToast("Failed to recognize text due to ${it.message}")

            }

        } catch (e: Exception) {

            showToast("Faild to prepare image due to ${e.message}")

        }

    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun showInputImageDialog() {

        val popupMenu = PopupMenu(this, binding.btnImage)

        popupMenu.menu.add(Menu.NONE, 1, 1, "CAMERA")
        popupMenu.menu.add(Menu.NONE, 2, 2, "GALLERY")

        popupMenu.show()

        popupMenu.setOnMenuItemClickListener { menuItem ->


            val id = menuItem.itemId
            if (id == 1) {

                if (checkCameraPermission()) {

                    pickImageCamera()

                } else {

                    requestCameraPermission()

                }

            } else if (id == 2) {

                if (checkStoragePermission()) {

                    requestStoragePermission()
                    pickImageGallery()

                } else {

                    requestStoragePermission()

                }

            }

            return@setOnMenuItemClickListener true

        }

    }

    private fun pickImageGallery() {

        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        galleryActivityResultLauncher.launch(intent)
    }

    private val galleryActivityResultLauncher =


        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {

                val data = result.data
                imageUri = data!!.data!!
                binding.imageTV.setImageURI(imageUri)

            } else {

                showToast("Cancelled...")

            }

        }

    private fun pickImageCamera() {

        val values = ContentValues()
        values.put(MediaStore.Images.Media.TITLE, "Sample Title")
        values.put(MediaStore.Images.Media.DESCRIPTION, "Sample Description")

        imageUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)!!

        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
        cameraActivityResult.launch(intent)


    }

    private val cameraActivityResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->

            if (result.resultCode == Activity.RESULT_OK) {


                binding.imageTV.setImageURI(imageUri)

            } else {

                showToast("Cancelled...")

            }

        }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun checkStoragePermission(): Boolean {

        return ContextCompat.checkSelfPermission(
            this, Manifest.permission.READ_MEDIA_IMAGES
        ) == PackageManager.PERMISSION_GRANTED

    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun checkCameraPermission(): Boolean {

        val cameraResult = return ContextCompat.checkSelfPermission(
            this, Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

        val storageResult = return ContextCompat.checkSelfPermission(
            this, Manifest.permission.READ_MEDIA_IMAGES
        ) == PackageManager.PERMISSION_GRANTED

        return cameraResult && storageResult

    }

    private fun requestStoragePermission() {

        ActivityCompat.requestPermissions(this, storagePermission, STORAGE_REQUEST_CODE)

    }

    private fun requestCameraPermission() {

        ActivityCompat.requestPermissions(this, cameraPermission, CAMERA_REQUEST_CODE)

    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {


            CAMERA_REQUEST_CODE -> {

                if (grantResults.isNotEmpty()) {

                    val cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED
                    val storageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED

                    if (cameraAccepted && storageAccepted) {

                        pickImageCamera()

                    } else {

                        showToast("Camera & Storage Permission are required...")

                    }

                }

            }

            STORAGE_REQUEST_CODE -> {

                if (grantResults.isNotEmpty()) {

                    val storageAccepted = grantResults[0] === PackageManager.PERMISSION_GRANTED

                    if (storageAccepted) {

                        pickImageGallery()

                    }

                } else {

                    showToast("Storage Permission is required...")

                }

            }

        }
    }

    private fun showToast(message: String) {

        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()

    }
}