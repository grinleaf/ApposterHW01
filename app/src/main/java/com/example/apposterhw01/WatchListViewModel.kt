package com.example.apposterhw01

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

class WatchListViewModel:ViewModel() {
    //라이브데이터가 필요없는? 거 아님? 그럼 뷰모델을 정말 뷰모델스코프 사용해서 데이터 캐싱하기 위한 용도 그 이상 그 이하도 아닌 것?
    //근데 가이드문서에서는 뷰모델 영역에서 쓰라고 되어있긴 함,,,
    val watchListLiveData = MutableLiveData<List<String>>()
    val watchRepository= WatchRepository()

    fun getPreview() : Flow<PagingData<Preview>> {
        return watchRepository.getWatchPreview().cachedIn(viewModelScope)
//        watchRepository.
//        run {
//            requestWatchPreview()
//            .subscribe({
//                val previewList = it.body()?.watchSells?.map {
//                    watchRepository.baseUrl+it.watch.images.preview
//                }
//                watchListLiveData.value = previewList
//            },{
//                Log.i("aaa","${it.message}")
//            })
//            getWatchPreview().cachedIn(viewModelScope)
//        }
    }
}