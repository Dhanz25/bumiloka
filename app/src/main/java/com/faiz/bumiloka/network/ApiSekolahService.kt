package com.faiz.bumiloka.network

import com.faiz.bumiloka.model.SekolahResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiSekolahService {
    @GET("sekolah/smp")
    suspend fun getSmpByKabupaten(
        @Query("kab_kota") kabupaten: String,
        @Query("page") page: Int = 1,
        @Query("perPage") perPage: Int = 1000
    ): SekolahResponse

    @GET("sekolah/sma")
    suspend fun getSmaByKabupaten(
        @Query("kab_kota") kabupaten: String,
        @Query("page") page: Int = 1,
        @Query("perPage") perPage: Int = 1000
    ): SekolahResponse

    @GET("sekolah/smk")
    suspend fun getSmkByKabupaten(
        @Query("kab_kota") kabupaten: String,
        @Query("page") page: Int = 1,
        @Query("perPage") perPage: Int = 1000
    ): SekolahResponse
}