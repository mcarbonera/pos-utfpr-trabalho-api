package com.example.myapitest

import androidx.core.app.ActivityCompat.requestPermissions

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat.checkSelfPermission
import androidx.core.content.FileProvider
import androidx.core.content.PermissionChecker.PERMISSION_GRANTED
import com.example.myapitest.databinding.ActivityNewCarBinding
import com.example.myapitest.model.CarroValor
import com.example.myapitest.model.Localizacao
import com.example.myapitest.service.RetrofitClient
import com.example.myapitest.service.requestApi
import com.example.myapitest.service.Result
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.security.SecureRandom
import java.text.SimpleDateFormat
import java.util.Date
import java.util.UUID

class NewCarActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNewCarBinding

    private lateinit var imageUri: Uri
    private var imageFile: File? = null
    private var imageButtonEnabled: Boolean = true

    private val cameraLauncher: ActivityResultLauncher<Intent> = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if(result.resultCode == Activity.RESULT_OK) {
            binding.imageUrl.setText("Imagem inserida!")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNewCarBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        //setupGoogleMap()
        setupView()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if(requestCode == CAMERA_REQUEST_CODE) {
            if(grantResults.isNotEmpty() && grantResults[0] == PERMISSION_GRANTED) {
                openCamera()
            } else {
                toastMessage("Permissão de Câmera Negada")
            }
        }
    }

    private fun setupView() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
        binding.saveCta.setOnClickListener {
            saveCarro()
        }
        binding.takePictureCta.setOnClickListener {
            takePicture()
        }
    }

    private fun saveCarro() {
        if(!validateForm()) return

        uploadImageToFirebase()
    }

    private fun takePicture() {
        if(checkSelfPermission(this, android.Manifest.permission.CAMERA) == PERMISSION_GRANTED) {
            openCamera()
        } else {
            requestCameraPermission()
        }
    }

    private fun openCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        imageUri = createImageUri()
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
        cameraLauncher.launch(intent)
    }

    private fun createImageUri(): Uri {
        val timeStamp: String = SimpleDateFormat("yyyMMdd_HHmmss").format(Date())
        val imageFileName = "JPEG_${timeStamp}_"

        val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)

        imageFile = File.createTempFile(
            imageFileName,
            ".jpg",
            storageDir
        )

        return FileProvider.getUriForFile(
            this,
            "com.example.myapitest.fileprovider",
            imageFile!!
        )
    }

    private fun requestCameraPermission() {
        requestPermissions(
            this,
            arrayOf(android.Manifest.permission.CAMERA),
            CAMERA_REQUEST_CODE
        )
    }

    private fun validateForm(): Boolean {
        if(binding.name.text.toString().isBlank()) {
            toastMessage(getString(R.string.error_validate_form, "Nome"))
            return false
        }
        if(binding.year.text.toString().isBlank()) {
            toastMessage(getString(R.string.error_validate_form, "Ano"))
            return false
        }
        if(binding.license.text.toString().isBlank()) {
            toastMessage(getString(R.string.error_validate_form, "Placa"))
            return false
        }
        if(imageFile == null) {
            toastMessage(getString(R.string.error_validate_take_picture))
            return false
        }
        return true
    }

    private fun uploadImageToFirebase() {
        val storageRef = FirebaseStorage.getInstance().reference

        val imagesRef = storageRef.child("images/${UUID.randomUUID()}.jpg")

        val baos = ByteArrayOutputStream()
        val imageBitmap = BitmapFactory.decodeFile(imageFile!!.path)
        imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val data = baos.toByteArray()

        toggleImageButtonEnabled()
        imagesRef.putBytes(data)
            .addOnFailureListener {
                toggleImageButtonEnabled()
                toastMessage(getString(R.string.upload_failed))
            }
            .addOnSuccessListener {
                toggleImageButtonEnabled()
                imagesRef.downloadUrl.addOnSuccessListener { uri ->
                    saveData(uri.toString())
                }
            }
    }

    private fun toggleImageButtonEnabled() {
        if(imageButtonEnabled) {
            binding.loadImageProgress.visibility = View.VISIBLE
            binding.takePictureCta.isEnabled = false
            binding.saveCta.isEnabled = false
            imageButtonEnabled = false
        } else {
            binding.loadImageProgress.visibility = View.GONE
            binding.takePictureCta.isEnabled = true
            binding.saveCta.isEnabled = true
            imageButtonEnabled = true
        }
    }

    private fun saveData(imageUrl: String) {
        val name = binding.name.text.toString()
        val year = binding.year.text.toString()
        val license = binding.license.text.toString()

        CoroutineScope(Dispatchers.IO).launch {
            val id = SecureRandom().nextInt().toString()
            val carro = CarroValor(
                id,
                name,
                year,
                licence = license,
                imageUrl,
                place = Localizacao(0.0, 0.0) //place
            )
            val result = requestApi { RetrofitClient.carroService.addCarros(carro) }
            withContext(Dispatchers.Main) {
                when(result) {
                    is Result.Error -> onSaveError()
                    is Result.Success -> {
                        onSaveSuccess()
                        finish()
                    }
                }
            }
        }
    }

    private fun onSaveError() {
        toastMessage(getString(R.string.erro_ao_criar_o_registro))
    }

    private fun onSaveSuccess() {
        toastMessage(getString(R.string.registro_inserido_com_sucesso))
    }

    private fun toastMessage(mensagem: String) {
        Toast.makeText(
            this@NewCarActivity,
            mensagem,
            Toast.LENGTH_SHORT
        ).show()
    }

    companion object {
        private const val CAMERA_REQUEST_CODE = 100
        fun newIntent(context: Context) =
            Intent(context, NewCarActivity::class.java)
    }
}