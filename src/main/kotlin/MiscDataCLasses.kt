package com.example

data class TeachersTotalFilledTimes(
    val teacherName: String, val times: ArrayList<JustTimeDataCLass>
)


data class JustTimeDataCLass(val startTime: String, val endTime: String, val dayName: String)


data class GroupsTotalFilledTimes(val groupYear: Int, val times: ArrayList<JustTimeDataCLass>);