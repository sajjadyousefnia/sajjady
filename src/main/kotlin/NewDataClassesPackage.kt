package com.example

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class FirstClass(
    @SerializedName("requested")
    @Expose
    val generalList: GeneralList
)

data class GeneralList(
    @SerializedName("workingTime")
    @Expose
    val workingTime: workingTimeDataClass,
    @SerializedName("entriesYears")
    @Expose
    val entriesYears: ArrayList<Int>,
    @SerializedName("teachersNames")
    @Expose
    val teachersNames: ArrayList<TeachersInfoDataClass>,
    @SerializedName("courseGroups")
    @Expose
    val courseGroups: ArrayList<CourseGroupsDataClass>,
    @SerializedName("educationGroupName")
    @Expose
    val educationGroupName: String,
    @SerializedName("classes")
    @Expose
    val classes: ArrayList<String>


)

/*data class Classes(
    @SerializedName("classNumber")
    @Expose
    val classes: String
)*/

data class workingTimeDataClass(
    @SerializedName("startTime")
    @Expose
    val startTime: String,
    @SerializedName("endTime")
    @Expose
    val endTime: String
)

data class CourseGroupsDataClass(
    @SerializedName("courseYear")
    @Expose
    val courseYear: Int,
    @SerializedName("coach")
    @Expose
    val coach: String,
    @SerializedName("presentedCourses")
    @Expose
    val presentedCourses: ArrayList<CourseDataClass>
)

data class CourseDataClass(
    @SerializedName("teacher")
    @Expose
    val teacher: String,
    @SerializedName("courseName")
    @Expose
    val courseName: String,
    @SerializedName("units")
    @Expose
    val units: Int,
    @SerializedName("recurrences")
    @Expose
    val recurrences: Int,
    @SerializedName("id")
    @Expose
    val id: Int,
    @SerializedName("courseType")
    @Expose
    val courseType: String,
    @SerializedName("groupYear")
    @Expose
    val groupYear: ArrayList<Int>
)

data class TeachersInfoDataClass(
    @SerializedName("teacherName")
    @Expose
    val teacherName: String,
    @SerializedName("openDays")
    @Expose
    val openDays: ArrayList<OpenDaysDataClass>
)

data class OpenDaysDataClass(
    @SerializedName("dayName")
    @Expose
    val dayName: String,
    @SerializedName("startTime")
    @Expose
    val startTime: String,
    @SerializedName("endTime")
    @Expose
    val endTime: String
)
