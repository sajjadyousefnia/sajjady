package com.example

import java.time.Year

data class TeachersTotalFreeTimesDataClass(
    val teacherName: String, val times: ArrayList<JustTimeDataCLass>
)

data class TeachersTotalAssignedTimesDataClass(val teacherName: String, val time: JustTimeDataCLass)

data class JustTimeDataCLass(val startTime: String, val endTime: String, val dayName: String)


data class GroupsTotalFilledTimes(val groupYear: Int, val times: ArrayList<JustTimeDataCLass>);


data class OutputDataClass(
    val courseName: String, val startTime: String, val endTime: String
    , val dayName: String, val year: Int, val roomNumber: Int
    , val teacherName: String
)

data class RoomsDatalasses(
    val roomNumber: Int,
    val data: JustTimeDataCLass
)

data class GroupDataClass(
    val groupYear: Int,
    val data: JustTimeDataCLass
)