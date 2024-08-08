package com.example.myimagesearch.model

import com.google.gson.annotations.SerializedName

data class ImageSearch(
    @SerializedName("meta")
    val metaData: MetaData?,
    @SerializedName("documents")
    var documents: MutableList<MediaDocument>?
)