package com.example.climahoy.data.network

import com.google.gson.annotations.SerializedName
data class ErrorAPI (
    @SerializedName("type"    ) var type    : String? = null,
    @SerializedName("title"   ) var title   : String? = null,
    @SerializedName("status"  ) var status  : Int?    = null,
    @SerializedName("errors"  ) var errors  : Map<String, List<String>> = emptyMap(),
    @SerializedName("traceId" ) var traceId : String? = null
)