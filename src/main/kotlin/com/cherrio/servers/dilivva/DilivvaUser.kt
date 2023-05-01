package com.cherrio.servers.dilivva


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DilivvaUser(
    @SerialName("status")
    val status: String = "",
    @SerialName("message")
    val message: String = "",
    @SerialName("data")
    val `data`: Data = Data()
)