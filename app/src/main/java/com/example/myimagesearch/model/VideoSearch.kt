package com.example.myimagesearch.model

import com.google.gson.annotations.SerializedName

data class VideoSearch(
    @SerializedName("meta")
    val metaData: MetaData?,
    @SerializedName("documents")
    val documents: List<MediaDocument>?
)