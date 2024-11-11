package com.example.myapitest.model

data class Carro(
    val id: String,
    val value: CarroValor
)

data class CarroValor (
    val id: String,
    val name: String,
    val year: String,
    val licence: String,
    val imageUrl: String,
    val place: Localizacao?
)