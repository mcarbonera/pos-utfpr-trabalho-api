package com.example.myapitest.service

import com.example.myapitest.model.Carro
import com.example.myapitest.model.CarroValor
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

interface CarroService {

    @GET("car")
    suspend fun getCarros(): List<CarroValor>

    @GET("car/{id}")
    suspend fun getCarro(@Path("id") id: String): Carro

    @DELETE("car/{id}")
    suspend fun deleteCarro(@Path("id") id: String)

    @POST("car")
    suspend fun addCarros(@Body carro: CarroValor): Carro

    @PATCH("car/{id}")
    suspend fun updateCarro(
        @Path("id") id: String,
        @Body carro: CarroValor
    ): Carro
}