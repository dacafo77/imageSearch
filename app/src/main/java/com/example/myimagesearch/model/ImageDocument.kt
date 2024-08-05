package com.example.myimagesearch.model

import java.util.Date

data class ImageDocument(
    val collection: String?,
    val dateTime: Date?,
    val displaySiteName: String?,
    val docUrl: String?,
    val height: Int?,
    val imageUrl: String?,
    val thumbnailUrl: String?,
    val width: Int?
)