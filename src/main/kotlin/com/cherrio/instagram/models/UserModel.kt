package com.cherrio.instagram.models
import kotlinx.serialization.Serializable

import kotlinx.serialization.SerialName


@Serializable
data class UserResponse(
    @SerialName("user")
    val user: User,
    @SerialName("status")
    val status: String
)

@Serializable
data class User(
    @SerialName("pk")
    val pk: String,
    @SerialName("username")
    val username: String,
    @SerialName("full_name")
    val fullName: String,
    @SerialName("media_count")
    val mediaCount: Int,
    @SerialName("follower_count")
    val followerCount: Int,
    @SerialName("following_count")
    val followingCount: Int,
    @SerialName("biography")
    val biography: String,
    @SerialName("address_street")
    val addressStreet: String? = null,
    @SerialName("business_contact_method")
    val businessContactMethod: String? = null,
    @SerialName("city_name")
    val cityName: String? = null,
    @SerialName("contact_phone_number")
    val contactPhoneNumber: String? = null,
    @SerialName("public_email")
    val publicEmail: String? = null,
    @SerialName("public_phone_country_code")
    val publicPhoneCountryCode: String? = null,
    @SerialName("public_phone_number")
    val publicPhoneNumber: String? = null,
    @SerialName("is_business")
    val isBusiness: Boolean,
    @SerialName("page_name")
    val pageName: String? = null,
    @SerialName("whatsapp_number")
    val whatsappNumber: String? = null,
    @SerialName("is_whatsapp_linked")
    val isWhatsappLinked: Boolean,
    @SerialName("external_url")
    val externalUrl: String
)

@Serializable
data class HdProfilePicVersion(
    @SerialName("width")
    val width: Int,
    @SerialName("height")
    val height: Int,
    @SerialName("url")
    val url: String
)

@Serializable
data class HdProfilePicUrlInfo(
    @SerialName("url")
    val url: String,
    @SerialName("width")
    val width: Int,
    @SerialName("height")
    val height: Int
)


@Serializable
data class BioLink(
    @SerialName("link_id")
    val linkId: String,
    @SerialName("url")
    val url: String,
    @SerialName("lynx_url")
    val lynxUrl: String,
    @SerialName("link_type")
    val linkType: String,
    @SerialName("title")
    val title: String,
    @SerialName("open_external_url_with_in_app_browser")
    val openExternalUrlWithInAppBrowser: Boolean
)