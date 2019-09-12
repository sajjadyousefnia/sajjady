package com.example

data class TeachersTotalFreeTimesDataClass(
    val teacherName: String, val times: ArrayList<JustTimeDataCLass>
)

data class TeachersTotalAssignedTimesDataClass(val teacherName: String, val time: JustTimeDataCLass)

data class JustTimeDataCLass(val startTime: String, val endTime: String, val dayName: String)


data class GroupsTotalFilledTimes(val groupYear: Int, val times: ArrayList<JustTimeDataCLass>);