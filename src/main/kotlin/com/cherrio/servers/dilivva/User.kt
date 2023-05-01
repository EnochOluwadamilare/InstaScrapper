package com.cherrio.servers.dilivva


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class User(
    @SerialName("id")
    val id: Int = 0,
    @SerialName("uuid")
    val uuid: String = "",
    @SerialName("first_name")
    val firstName: String = "",
    @SerialName("last_name")
    val lastName: String = "",
    @SerialName("full_name")
    val fullName: String = "",
    @SerialName("email")
    val email: String = "",
    @SerialName("phone")
    val phone: String = "",
    @SerialName("is_banned")
    val isBanned: Boolean = false,
    @SerialName("ban_comment")
    val banComment: String = "",
    @SerialName("ban_expiry")
    val banExpiry: String = "",
    @SerialName("email_verified_at")
    val emailVerifiedAt: String? = null,
    @SerialName("wallet")
    val wallet: Wallet? = null,
    @SerialName("created_at")
    val createdAt: String = "",
    @SerialName("updated_at")
    val updatedAt: String = ""
)

@Serializable
data class Sender(
    @SerialName("id")
    val id: Int = 0,
    @SerialName("full_name")
    val fullName: String = "",
    @SerialName("email")
    val email: String = "",
    @SerialName("phone")
    val phone: String = "",
    @SerialName("verified_email")
    val emailVerifiedAt: String? = null,
    @SerialName("sign_up_time")
    val createdAt: String = ""
)

fun toSender(user: User) = Sender(
    user.id, user.fullName, user.email, user.phone, if (user.emailVerifiedAt != null) "yes" else "no", user.createdAt
)

