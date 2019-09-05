package com.example

import com.google.gson.annotations.SerializedName

data class ExampleDataClass(
    @SerializedName("hello")
    val hello: String
);