package com.example.apposterhw01

import io.reactivex.Single
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface WatchService {

    @GET("/api/watch-sells/popular/weekly")
    fun getWatchList(
        @Query("skip") skip:Int,
        @Query("limit") limit:Int,
        @Query("withoutFree") withoutFree:Boolean
    ) : Single<Response<WatchResponse>>
}