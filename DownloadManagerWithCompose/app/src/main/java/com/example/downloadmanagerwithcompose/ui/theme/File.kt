package com.example.downloadmanagerwithcompose.ui.theme
data class File(
    val id:String,
    val name:String,
    val type:String,
    val url:String,
    var downloadedUri:String?=null,
    var isDownloading:Boolean = false,
)
