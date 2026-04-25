package com.faiz.bumiloka.network

import com.faiz.bumiloka.model.Wilayah
import retrofit2.http.GET
import retrofit2.http.Path

interface ApiService {

    @GET("provinces.json")
    suspend fun getProvinsi(): List<Wilayah>

    @GET("regencies/{id}.json")
    suspend fun getKabupaten(@Path("id") id: String): List<Wilayah>

    @GET("districts/{id}.json")
    suspend fun getKecamatan(@Path("id") id: String): List<Wilayah>
}