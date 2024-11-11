package com.example.myapitest

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.myapitest.databinding.ActivityCarroDetailBinding
import com.example.myapitest.model.Carro
import com.example.myapitest.service.RetrofitClient
import com.example.myapitest.service.requestApi
import com.example.myapitest.service.Result
import com.example.myapitest.ui.loadUrl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CarroDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCarroDetailBinding
    private lateinit var carro: Carro

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCarroDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupView()
        loadItem()
        setupMap()
    }

    private fun setupView() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
        binding.editCTA.setOnClickListener {
            editCarro()
        }
        binding.deleteCTA.setOnClickListener {
            deleteCarro()
        }
    }

    private fun loadItem() {
        val carroId = intent.getStringExtra(ARG_ID) ?: ""

        CoroutineScope(Dispatchers.IO).launch {
            val result = requestApi { RetrofitClient.carroService.getCarro(carroId) }
            withContext(Dispatchers.Main) {
                when(result) {
                    is Result.Error -> handleError()
                    is Result.Success -> {
                        carro = result.data
                        handleSuccess()
                    }
                }
            }
        }
    }

    private fun setupMap() {
        // TODO - MAP
    }

    private fun editCarro() {
        CoroutineScope(Dispatchers.IO).launch {
            val result = requestApi {
                RetrofitClient.carroService.updateCarro(
                    carro.id,
                    carro.value.copy(
                        name = binding.name.text.toString(),
                        year = binding.year.text.toString(),
                        licence = binding.license.text.toString()
                    )
                )
            }

            withContext(Dispatchers.Main) {
                when(result) {
                    is Result.Error -> toastMessage(getString(R.string.unknown_error))
                    is Result.Success -> {
                        toastMessage(getString(R.string.carro_atualizado))
                        finish()
                    }
                }
            }
        }
    }

    private fun deleteCarro() {
        CoroutineScope(Dispatchers.IO).launch {
            val result = requestApi { RetrofitClient.carroService.deleteCarro(carro.id) }

            withContext(Dispatchers.Main) {
                when(result) {
                    is Result.Error -> toastMessage(getString(R.string.erro_exclusao))
                    is Result.Success -> {
                        toastMessage(getString(R.string.registro_excluido))
                        finish()
                    }
                }
            }
        }
    }

    private fun handleError() {
        toastMessage(getString(R.string.unknown_error))
    }

    private fun handleSuccess() {
        binding.name.setText(carro.value.name)
        binding.year.setText(carro.value.year)
        binding.license.setText(carro.value.licence)

        binding.image.loadUrl(carro.value.imageUrl)
    }

    private fun toastMessage(mensagem: String) {
        Toast.makeText(
            this@CarroDetailActivity,
            mensagem,
            Toast.LENGTH_SHORT
        ).show()
    }

    companion object {
        private const val ARG_ID = "ARG_ID"

        fun newIntent(
            context: Context,
            carroId: String
        ) = Intent(context, CarroDetailActivity::class.java).apply {
            putExtra(ARG_ID, carroId)
        }
    }
}