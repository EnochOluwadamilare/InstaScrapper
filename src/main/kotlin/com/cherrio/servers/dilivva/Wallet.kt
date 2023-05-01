package com.cherrio.servers.dilivva


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Wallet(
    @SerialName("balance")
    val balance: String = ""
)