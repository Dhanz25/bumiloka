package com.faiz.bumiloka.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClientSekolah {

    private const val BASE_URL =
        "https://api-sekolah-indonesia.vercel.app/"

    val instance: ApiSekolahService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(
                GsonConverterFactory.create()
            )
            .build()
            .create(ApiSekolahService::class.java)
    }
}