package com.icaali.kalenderpuasasunnah.model

import com.google.gson.annotations.SerializedName

data class TanggalModel(
    @SerializedName("month") val month: String,
    @SerializedName("month_number") val monthNumber: Int,
    @SerializedName("puasa_code") val puasaCode: List<PuasaCode>,
    @SerializedName("title") val title: String,
    @SerializedName("desc") val desc: String,
    @SerializedName("tanggal_puasa") val tanggalPuasa: List<TanggalPuasa>
)

data class PuasaCode(@SerializedName("code") val code: Int)
