package com.example.myimagesearch.api

import com.example.myimagesearch.model.ImageSearch
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface ImageService {
    @GET("/v2/search/image")
    fun searchImages(
        @Header("Authorization") authHeader: String,
        @Query("query") query: String,
        @Query("sort") sort: String = "accuracy",
        @Query("page") page: Int = 1,
        @Query("size") size: Int = 10
    ): Call<ImageSearch>
}