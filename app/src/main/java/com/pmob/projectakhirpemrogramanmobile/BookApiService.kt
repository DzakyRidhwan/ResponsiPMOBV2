package com.pmob.projectakhirpemrogramanmobile.network

import retrofit2.http.GET
import retrofit2.http.Query
import com.pmob.projectakhirpemrogramanmobile.data.model.BookResponse


interface BookApiService {

    @GET("volumes")
    suspend fun searchBooks(
        @Query("q") query: String
    ): BookResponse
}
