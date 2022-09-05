package com.example.apposterhw01

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

class WatchRepository {
    val baseUrl = "http://mtm-api.apposter.com:7777"
    val skip = 9
    val limit = 8
    val withoutFree = true

    val retrofit by lazy {
        Retrofit.Builder().baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .build()
            .create(WatchService::class.java)
    }

//    fun requestWatchPreview() : Single<Response<WatchResponse>> {
//        return retrofit.getWatchList(skip, limit, withoutFree)
//            .subscribeOn(Schedulers.io())
//            .observeOn(AndroidSchedulers.mainThread())
//    }

    fun getWatchPreview(): Flow<PagingData<Preview>> {
        return Pager(
            config = PagingConfig(
                pageSize = 30,
//                maxSize = 100,
//                enablePlaceholders = false
            ),
            pagingSourceFactory = { MyPagingSource(retrofit, skip, limit, withoutFree)}
        ).flow
    }
}