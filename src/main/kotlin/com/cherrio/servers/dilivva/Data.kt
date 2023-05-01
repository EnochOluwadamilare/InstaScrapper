package com.cherrio.servers.dilivva


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Data(
    @SerialName("meta")
    val meta: Meta = Meta(),
    @SerialName("users")
    val users: List<User> = listOf()
)