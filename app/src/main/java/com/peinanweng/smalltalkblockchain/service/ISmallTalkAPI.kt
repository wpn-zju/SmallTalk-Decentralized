package com.peinanweng.smalltalkblockchain.service

import com.peinanweng.smalltalkblockchain.service.model.logic.SmallTalkApplication
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path

interface ISmallTalkAPI {
    @Multipart
    @POST("/upload/{path}")
    fun uploadFile(
        @Part file: MultipartBody.Part,
        @Path("path") path: String,
        @Part("desc") desc: RequestBody
    ): Call<Void>

    companion object {
        operator fun invoke(): ISmallTalkAPI {
            return Retrofit.Builder()
                .baseUrl(SmallTalkApplication.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(ISmallTalkAPI::class.java)
        }
    }
}
