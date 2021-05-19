package com.muslimApp.kalenderpuasasunnah.model

import com.google.gson.annotations.SerializedName

data class TanggalPuasa(
    @SerializedName("code") val code: Int,
    @SerializedName("tanggal") val tanggal: Long,
    @SerializedName("masehi") val masehi: String
)