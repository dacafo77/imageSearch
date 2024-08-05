package com.example.myimagesearch.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import java.util.Date
import java.util.UUID

@Parcelize
data class SearchModel(
    val id: String = UUID.randomUUID().toString(),
    val thumbnailUrl: String?,
    val siteName: String?,
    val datetime: Date?,
    val itemType: SearchListType,
    val isSaved: Boolean = false
) : Parcelable