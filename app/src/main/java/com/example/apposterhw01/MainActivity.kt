package com.example.apposterhw01

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.paging.*
import androidx.paging.LoadStateAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.example.apposterhw01.databinding.ActivityMainBinding
import com.example.apposterhw01.databinding.ListRetryHeaderFooterBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.random.Random


class MainActivity : AppCompatActivity() {
    val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    val previewImages = mutableListOf<String>()

//    lateinit var adapter: RVAdapter
//    lateinit var adapter: TestListAdapter
    lateinit var adapter: PagingAdapter
    lateinit var loadStateAdapter : com.example.apposterhw01.LoadStateAdapter

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


        // 3. 페이징 3 어댑터 + LoadStateAdapter
        adapter= PagingAdapter(Glide.with(this))
        loadStateAdapter= LoadStateAdapter{ adapter.retry() }
        binding.recyclerview.adapter = adapter
            .withLoadStateFooter(
                loadStateAdapter
            )

        //그리드뷰 Spansize 영역
        val gridLayoutManager = GridLayoutManager(this, 3)
        gridLayoutManager.spanSizeLookup= object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return if(position== adapter.itemCount && loadStateAdapter.itemCount>0) {
                    gridLayoutManager.spanCount
                }
//                else if(position== 0) {
//                    gridLayoutManager.spanCount
//                }
            else{
                    1
                }
            }
        }
        binding.recyclerview.layoutManager = gridLayoutManager

        lifecycleScope.launch {
            watchListViewModel.getPreview().collectLatest { pagingData ->
                adapter.run {
                    submitData(pagingData)
                    registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
                        override fun onItemRangeChanged(positionStart: Int, itemCount: Int) {
                            super.onItemRangeChanged(positionStart, itemCount)
                            if(adapter.itemCount == itemCount)
                            binding.recyclerview.smoothScrollToPosition(adapter.itemCount-1)  //얘 작동 안하는 이유 좀
                        }
                    })
                }
            }

//            adapter.loadStateFlow.collectLatest { loadStates ->
//                progressBar.isVisible = loadStates.refresh is LoadState.Loading
//                retry.sVisible = loadState.refresh !is LoadState.Loading
//                errorMsg.isVisible = loadState refresh is LoadState.Error
//            }

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


/* 3-1. paging3 adapter source (apply Coroutine) */

private const val STARTING_PAGE_INDEX = 1   //시작 요청 페이지 인덱스용

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

            // loadState 테스트를 위한 딜레이 5초
            delay(1)

            // loadState 테스트를 위한 랜덤 에러 발생
            if (Random.nextFloat() < 0.15) {
                throw Exception("Error : throw Exception")
            }

            Log.d("!!!!!", "load start key : ${params.key}")
            val position = params.key?: STARTING_PAGE_INDEX

//            val watchList = watchService.getWatchList(skip,limit,withoutFree).watchSells.map { it.watch.images }  //프디 인기순 리스트

            val newWatchList = watchService.getNewWatchList(limit=30 ,page=position, includeEvent = true).watchSells.map { it.watch.images }

            Log.d("!!!!!", "load start list : $newWatchList")
            Log.d("!!!!!", "position value : $position")

            LoadResult.Page(
                data = newWatchList,   //요기
                prevKey = if(position == STARTING_PAGE_INDEX) null else position-1, // 1이면 포지션 변화 x
                nextKey = if(newWatchList.isEmpty()) null else position+1           // 비어있으면 변화 x
            )
        } catch (exception: Exception){
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

    inner class WatchViewHolder(view: View): RecyclerView.ViewHolder(view){
        val iv:ImageView by lazy { view.findViewById(R.id.iv_list_item) }
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WatchViewHolder {
        return WatchViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.list_item, parent,false))
    }

    override fun onBindViewHolder(holder: WatchViewHolder, position: Int) {
        val imgUrl = WatchRepository().baseUrl + getItem(position)?.preview

        requestManager.load(imgUrl).into(holder.iv)
    }

    companion object {
        val diffUtil = object : DiffUtil.ItemCallback<Preview>() {
            override fun areItemsTheSame(oldItem: Preview, newItem: Preview) = oldItem == newItem
            override fun areContentsTheSame(oldItem: Preview, newItem: Preview) = oldItem == newItem
        }
    }
}

/* 4. LoadStateAdapter (optional) */

class LoadStateAdapter(private val retry: () -> Unit) : LoadStateAdapter<LoadStateViewHolder>(){
    override fun onCreateViewHolder(
        parent: ViewGroup,
        loadState: LoadState
    ) = LoadStateViewHolder(parent, retry)

    override fun onBindViewHolder(
        holder: LoadStateViewHolder,
        loadState: LoadState
    ) = holder.bind(loadState)


}

class LoadStateViewHolder(parent: ViewGroup, retry: () -> Unit)
    : RecyclerView.ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.list_retry_header_footer,parent,false)){
    private val binding = ListRetryHeaderFooterBinding.bind(itemView)
    private val progressBar = binding.progressBar
    private val retry = binding.retry.also { it.setOnClickListener { retry() } }
    private val errorMsg = binding.errorMsg

    fun bind(loadState: LoadState){
        //데이터 로딩중
        if(loadState is LoadState.Loading){
            itemView.background = null
        }

        //데이터 로드 에러 상황
        if(loadState is LoadState.Error){
            errorMsg.text = loadState.error.localizedMessage
            itemView.background = null
        }
        progressBar.isVisible = loadState !is LoadState.Error
        retry.isVisible = loadState is LoadState.Error
        errorMsg.isVisible = loadState is LoadState.Error
    }
}