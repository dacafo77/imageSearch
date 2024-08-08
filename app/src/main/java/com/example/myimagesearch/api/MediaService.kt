package com.example.myimagesearch.api

import com.example.myimagesearch.model.ImageSearch
import com.example.myimagesearch.model.VideoSearch
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface MediaService {
    //이미지 검색 메서드
    @GET("/v2/search/image")
    fun searchImages(
        @Header("Authorization") authHeader: String,
        @Query("query") query: String,
        @Query("sort") sort: String = "accuracy",
        @Query("page") page: Int = 1,
        @Query("size") size: Int = 10
    ): Call<ImageSearch>

    //동영상 검색 메서드
    @GET("/v2/search/vclip")
    fun searchVideos(
        @Header("Authorization") authHeader: String,
        @Query("query") query: String,
        @Query("sort") sort: String = "accuracy",
        @Query("page") page: Int = 1,
        @Query("size") size: Int = 10
    ): Call<VideoSearch>
}
