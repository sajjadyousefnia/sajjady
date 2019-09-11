package com.example

import com.google.gson.annotations.SerializedName

data class FirstClass(
    @SerializedName("requested")
    val generalList: GeneralList
)

data class GeneralList(
    @SerializedName("entriesYears")
    val entriesYears: ArrayList<Int>,
    @SerializedName("teachersNames")
    val teachersNames: ArrayList<TeachersInfoDataClass>,
    @SerializedName("courseGroups")
    val courseGroups: ArrayList<CourseGroupsDataClass>,
    @SerializedName("educationGroupName")
    val educationGroupName: String,
    @SerializedName("classesCount")
    val classesCount: Int


)

data class CourseGroupsDataClass(
    @SerializedName("courseYear")
    val courseYear: Int,
    @SerializedName("coach")
    val coach: String,
    @SerializedName("presentedCourses")
    val presentedCourses: ArrayList<CourseDataClass>
)

data class CourseDataClass(
    @SerializedName("teacher")
    val teacher: String,
    @SerializedName("courseName")
    val courseName: String,
    @SerializedName("units")
    val units: Int,
    @SerializedName("recurrences")
    val recurrences: Int
)

data class TeachersInfoDataClass(
    @SerializedName("teacherName")
    val teacherName: String,
    @SerializedName("openDays")
    val openDays: ArrayList<OpenDaysDataClass>
)

data class OpenDaysDataClass(
    @SerializedName("dayName")
    val dayName: String,
    @SerializedName("startTime")
    val startTime: String,
    @SerializedName("endTime")
    val endTime: String
)
