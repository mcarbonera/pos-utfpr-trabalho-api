package com.example.myapitest

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapitest.adapter.CarroAdapter
import com.example.myapitest.databinding.ActivityMainBinding
import com.example.myapitest.model.Carro
import com.example.myapitest.model.CarroValor
import com.example.myapitest.service.RetrofitClient
import com.example.myapitest.service.requestApi
import com.example.myapitest.service.Result
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupView()
    }

    override fun onResume() {
        super.onResume()
        fetchItems()
    }

    private fun setupView() {
        binding.recyclerView.layoutManager = LinearLayoutManager(this)

        binding.swipeRefreshLayout.setOnRefreshListener {
            binding.swipeRefreshLayout.isRefreshing = true
            fetchItems()
        }

        binding.addCta.setOnClickListener {
            startActivity(NewCarActivity.newIntent(this))
        }
        binding.btnLogout.setOnClickListener {
            logout()
        }
    }

    private fun logout() {
        FirebaseAuth.getInstance().signOut()
        val intent = LoginActivity.newIntent(this)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun fetchItems() {
        CoroutineScope(Dispatchers.IO).launch {
            val result = requestApi { RetrofitClient.carroService.getCarros() }

            withContext(Dispatchers.Main) {
                binding.swipeRefreshLayout.isRefreshing = false
                when(result) {
                    is Result.Error -> handleError()
                    is Result.Success -> handleSuccess(result.data)
                }
            }
        }
    }

    private fun handleError() {
        Toast.makeText(
            this,
            "Ocorreu um erro ao consultar",
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun handleSuccess(carros: List<CarroValor>) {
        val adapter = CarroAdapter(carros) {
            startActivity(CarroDetailActivity.newIntent(this, it.id))
        }
        binding.recyclerView.adapter = adapter
    }

    companion object {
        fun newIntent(context: Context) = Intent(context, MainActivity::class.java)
    }
}
