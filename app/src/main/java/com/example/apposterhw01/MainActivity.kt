package com.example.apposterhw01

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import androidx.paging.*
import androidx.paging.rxjava2.RxPagingSource
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.example.apposterhw01.databinding.ActivityMainBinding
import io.reactivex.Single
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException


class MainActivity : AppCompatActivity() {
    val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    val previewImages = mutableListOf<String>()
//    lateinit var adapter: RVAdapter
//    lateinit var adapter: TestListAdapter
    lateinit var adapter: PagingAdapter

    val watchListViewModel by lazy {
        ViewModelProvider(this).get(WatchListViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.run {
            title = "테스트 리스트"
            setDisplayShowTitleEnabled(false)
        }

        // 1. 기본 리사이클러뷰 어댑터
//        adapter = RVAdapter(Glide.with(this), previewImages)
//        binding.recyclerview.adapter = adapter

        // 2. 리스트 어댑터
//        adapter = TestListAdapter(Glide.with(this))
//        binding.recyclerview.adapter= adapter

        // 3. 페이징 3 어댑터
        adapter= PagingAdapter(Glide.with(this))
        binding.recyclerview.adapter = PagingAdapter(Glide.with(this))

        lifecycleScope.launch {
            watchListViewModel.getPreview().collectLatest { pagingData ->
                Log.d("!!!!!", "submitData start")
                adapter.submitData(pagingData)
                Log.d("!!!!!", "submitData end")
            }

//            watchListViewModel.run {
//                watchListLiveData.observe(this@MainActivity){
//                    previewImages.addAll(it)
////                adapter.notifyDataSetChanged()
//                    adapter.submitData(lifecycle, PagingData.from(previewImages))
//
//                    val pagingData= PagingData.from(it)
//                    adapter.submitData(this@MainActivity.lifecycle, pagingData)
//                }
//                getPreview()
//            }
        }
    }
}


/* 3-2. paging3 adapter source (apply RxJava) */

//class MyPagingSource(
//    val watchService: WatchService,
//    val skip : Int,
//    val limit : Int,
//    val withoutFree : Boolean
//): RxPagingSource<String, Preview>(){
//    override fun loadSingle(params: LoadParams<String>): Single<LoadResult<String, Preview>> {
//        return watchService.getWatchList(skip, limit, withoutFree).map<LoadResult<String, Preview>?> { result ->
//            val watchList = result.body()?.watchSells?.map { it.watch.images } ?: emptyList()
//            LoadResult.Page(
//                data = watchList,
//                prevKey = null,
//                nextKey = null
//            )
//        }
//            .onErrorReturn { e->
//                when (e){
//                    is IOException -> LoadResult.Error(e)
//                    is HttpException -> LoadResult.Error(e)
//                    else -> throw e
//                }
//            }
//    }
//
//    override fun getRefreshKey(state: PagingState<String, Preview>): String? {
//        return state.anchorPosition?.let { state.closestItemToPosition(it)?.preview }
//    }
//}


/* 3-1. paging3 adapter source (not RxJava) */

private const val STARTING_PAGE_INDEX = 1

class MyPagingSource(
    val watchService: WatchService,
    val skip : Int,
    val limit : Int,
    val withoutFree : Boolean
): PagingSource<Int, Preview>() {

    override fun getRefreshKey(state: PagingState<Int, Preview>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey?.plus(STARTING_PAGE_INDEX) ?: anchorPage?.nextKey?.minus(STARTING_PAGE_INDEX)
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Preview> {
        return try {
            Log.d("!!!!!", "load start key : ${params.key}")

            val position = params.key?: STARTING_PAGE_INDEX
            val watchList = watchService.getWatchList(skip,limit,withoutFree).watchSells.map { it.watch.images }

            Log.d("!!!!!", "load start list : $watchList")

            LoadResult.Page(
                data = watchList,   //요기
                prevKey = if(position == STARTING_PAGE_INDEX) null else position-1,
                nextKey = null
            )
        } catch (exception: IOException){
            LoadResult.Error(exception)
        } catch (exception: HttpException){
            LoadResult.Error(exception)
        }

    }
}

/* 1. RecyclerView Adapter */

//class RVAdapter(val requestManager: RequestManager, val previewImage: MutableList<String>) : RecyclerView.Adapter<RVAdapter.VH>() {
//    inner class VH(view:View):RecyclerView.ViewHolder(view){
//        val iv= view.findViewById<ImageView>(R.id.iv_list_item)
//    }
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
//        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.list_item, parent, false)
//        return VH(itemView)
//    }
//
//    override fun onBindViewHolder(holder: VH, position: Int) {
//        requestManager.load(previewImage[position]).into(holder.iv)
//    }
//
//    override fun getItemCount(): Int = previewImage.size
//}


/* 2. list adapter */

//class TestListAdapter(val requestManager: RequestManager) : ListAdapter<String, TestListAdapter.VH>(diffUtil){
//    inner class VH(view:View):RecyclerView.ViewHolder(view){
//        val iv:ImageView by lazy { view.findViewById(R.id.iv_list_item) }
//    }
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
//        val itemView= LayoutInflater.from(parent.context).inflate(R.layout.list_item, parent, false)
//        return VH(itemView)
//    }
//
//    override fun onBindViewHolder(holder: VH, position: Int) {
//        requestManager.load(getItem(position)).into(holder.iv)
//    }
//
//    companion object {
//        val diffUtil = object : DiffUtil.ItemCallback<String>() {
//            override fun areContentsTheSame(oldItem: String, newItem: String) = oldItem == newItem
//            override fun areItemsTheSame(oldItem: String, newItem: String) = oldItem == newItem
//        }
//    }
//}


/* 3. paging3 adapter */

class PagingAdapter(val requestManager: RequestManager) : PagingDataAdapter<Preview, PagingAdapter.WatchViewHolder>(diffUtil) {
    class WatchViewHolder(view: View): RecyclerView.ViewHolder(view){
        val iv:ImageView by lazy { view.findViewById(R.id.iv_list_item) }
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WatchViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.list_item, parent,false)
        return WatchViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: WatchViewHolder, position: Int) {
        val imgUrl = "http://mtm-api.apposter.com:7777"+getItem(position)?.preview

        Log.d("!!!!!", "url : $imgUrl")

        requestManager.load(imgUrl).into(holder.iv)
    }

    companion object {
        val diffUtil = object : DiffUtil.ItemCallback<Preview>() {
            override fun areItemsTheSame(oldItem: Preview, newItem: Preview) = oldItem == newItem
            override fun areContentsTheSame(oldItem: Preview, newItem: Preview) = oldItem == newItem
        }
    }
}