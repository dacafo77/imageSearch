package com.example.myimagesearch.model

import com.google.gson.annotations.SerializedName
import java.util.Date

data class ImageDocument(
    @SerializedName("collection")
    val collection: String?,
    @SerializedName("datetime")
    val dateTime: Date?,
    @SerializedName("display_sitename")
    val displaySiteName: String?,
    @SerializedName("doc_url")
    val docUrl: String?,
    @SerializedName("height")
    val height: Int?,
    @SerializedName("image_url")
    val imageUrl: String?,
    @SerializedName("thumbnail_url")
    val thumbnailUrl: String?,
    @SerializedName("width")
    val width: Int?
)