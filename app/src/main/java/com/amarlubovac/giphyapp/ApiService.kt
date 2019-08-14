package com.amarlubovac.giphyapp

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.*

interface ApiService {

    @GET("v1/gifs/trending")
    fun getTrending(
        @Query("api_key") apiKey: String,
        @Query("limit") limit: Int,
        @Query("rating") rating: String): Call<DataModel>

    @GET("v1/gifs/search")
    fun searchGif(
        @Query("api_key") apiKey: String,
        @Query("q") query: String,
        @Query("limit") limit: Int,
        @Query("offest") offset: Int,
        @Query("rating") rating: String): Call<DataModel>

    @Multipart
    @POST("https://upload.giphy.com/v1/gifs")
    fun uploadGiff(
        @Part file: MultipartBody.Part?,
        @Part("api_key") apiKey: RequestBody?,
        @Part("tags") tags: RequestBody?): Call<Any>
}