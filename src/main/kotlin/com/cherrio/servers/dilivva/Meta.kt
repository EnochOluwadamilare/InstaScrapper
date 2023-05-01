package com.cherrio.servers.dilivva


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Meta(
    @SerialName("total")
    val total: Int = 0,
    @SerialName("per_page")
    val perPage: Int = 0,
    @SerialName("current_page")
    val currentPage: Int = 0,
    @SerialName("last_page")
    val lastPage: Int = 0,
    @SerialName("first_page_url")
    val firstPageUrl: String = "",
    @SerialName("last_page_url")
    val lastPageUrl: String = "",
    @SerialName("next_page_url")
    val nextPageUrl: String = "",
    @SerialName("prev_page_url")
    val prevPageUrl: String? = null,
    @SerialName("path")
    val path: String = "",
    @SerialName("from")
    val from: Int = 0,
    @SerialName("to")
    val to: Int = 0
)