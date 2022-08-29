package com.example.apposterhw01

data class WatchResponse(val count: Int, val watchSells: MutableList<Watch>)

data class Watch(val watch:Images)

data class Images(val images:Preview)

data class Preview(val preview:String)