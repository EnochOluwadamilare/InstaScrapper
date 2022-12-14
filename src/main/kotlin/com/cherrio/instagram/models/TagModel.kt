package com.cherrio.instagram.models
import kotlinx.serialization.Serializable

import kotlinx.serialization.SerialName



@Serializable
data class TagResponse(
    @SerialName("count")
    val count: Int,
    @SerialName("data")
    val data: Data,
    @SerialName("status")
    val status: String
)

@Serializable
data class Data(
    @SerialName("recent")
    val recent: Recent,
)


@Serializable
data class Recent(
    @SerialName("sections")
    val sections: List<SectionX>,
    @SerialName("more_available")
    val moreAvailable: Boolean,
    @SerialName("next_max_id")
    val nextMaxId: String,
    @SerialName("next_page")
    val nextPage: Int,
)





@Serializable
data class SectionX(
    @SerialName("layout_content")
    val layoutContent: LayoutContentX,
)

@Serializable
data class LayoutContentX(
    @SerialName("medias")
    val medias: List<MediaXX>
)

@Serializable
data class MediaXX(
    @SerialName("media")
    val media: InnerMedia
)

@Serializable
data class InnerMedia(
    @SerialName("user")
    val user: MediaUser
)

@Serializable
data class MediaUser(
    @SerialName("pk")
    val pk: String,
    @SerialName("username")
    val username: String
)