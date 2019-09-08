package com.example

import com.google.gson.annotations.SerializedName


class FirstClass(
    @SerializedName("requested")
    val requested: ArrayList<GeneralClass>
)

class GeneralClass(
    @SerializedName("courseName")
    val courseName: String,
    @SerializedName("data")
    val data: ArrayList<DaysData>,
    @SerializedName("recurrences")
    val recurrences: Int
);

data class DaysData(
    @SerializedName("day")
    val day: String
    , @SerializedName("repeats")
    val repeats: ArrayList<RepeatClass>
)

data class RepeatClass(
    @SerializedName("startTime")
    val startTime: String,
    @SerializedName("endTime")
    val endTime: String
)
