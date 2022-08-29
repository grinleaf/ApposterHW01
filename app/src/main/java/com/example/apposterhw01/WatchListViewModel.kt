package com.example.apposterhw01

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import androidx.paging.rxjava2.cachedIn
import androidx.paging.rxjava2.flowable
import androidx.paging.rxjava2.observable

class WatchListViewModel:ViewModel() {
    //라이브데이터가 필요없는? 거 아님? 그럼 뷰모델을 정말 뷰모델스코프 사용해서 데이터 캐싱하기 위한 용도 그 이상 그 이하도 아닌 것?
    val watchListLiveData = MutableLiveData<List<String>>()
    val watchRepository= WatchRepository()

    fun getPreview(){
        watchRepository.run {
//            requestWatchPreview()
//            .subscribe({
//                val previewList = it.body()?.watchSells?.map {
//                    watchRepository.baseUrl+it.watch.images.preview
//                }
//                watchListLiveData.value = previewList
//            },{
//                Log.i("aaa","${it.message}")
//            })
            getWatchPreview().cachedIn(viewModelScope)
        }
    }
}