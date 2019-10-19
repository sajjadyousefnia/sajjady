package calculations

import com.example.CourseDataClass
import com.example.FirstClass
import com.example.operatingDates
import org.optaplanner.core.api.score.Score
import org.optaplanner.core.api.score.buildin.hardsoft.HardSoftScore
import org.optaplanner.core.impl.score.director.incremental.AbstractIncrementalScoreCalculator
import java.time.LocalDateTime
import java.time.LocalTime

class IncrementalScoreCalculator : AbstractIncrementalScoreCalculator<CourseSchedule>() {
    var hardScore = 0
    var softScore = 0
    lateinit var TotalJson: FirstClass
    val occupiedTeacherBreaks = hashSetOf<String>()
    val occupiedGroupBreaks = hashSetOf<String>()
    val occupiedTeacherEntryBreaks = arrayListOf<String>()
    val occupiedRoomBreaks = hashSetOf<String>()
    val initializedCourses = hashSetOf<String>()
    val motherCourses = arrayListOf<CourseDataClass>()
    var isFirstTime = false
    override fun resetWorkingSolution(courseSchedule: CourseSchedule) {
        softScore = 0
        hardScore = 0
        TotalJson = courseSchedule.totalJson
        /*occupiedTeacherBreaks.clear()
        occupiedGroupBreaks.clear()
        motherCourses.clear()
        occupiedRoomBreaks.clear()
        occupiedTeacherEntryBreaks.clear()
        initializedCourses.clear()*/
        if (!isFirstTime) {
            if (!courseSchedule.lectureList.isNullOrEmpty()) {
                courseSchedule.totalJson.generalList.courseGroups.forEach {
                    it.presentedCourses.forEach {
                        motherCourses.add(it)
                    }
                }

                val all = generateSequence(operatingDates.start.atStartOfDay()) { dt ->
                    dt.plusMinutes(15).takeIf { it.plusMinutes(15) <= operatingDates.endInclusive.atTime(23, 59) }
                }.map { it..it.plusMinutes(15) }.toMutableList()
                all.removeAll {
                    it.start.toLocalTime().isBefore(
                        LocalTime.of(
                            TotalJson.generalList.workingTime.startTime.split(":")[0].toInt(),
                            TotalJson.generalList.workingTime.startTime.split(":")[1].toInt()
                        )
                    ) ||
                            it.endInclusive.toLocalTime().isAfter(
                                LocalTime.of(
                                    TotalJson.generalList.workingTime.endTime.split(":")[0].toInt(),
                                    TotalJson.generalList.workingTime.endTime.split(":")[1].toInt()
                                )
                            )
                }
                val allTimes = all.flatMap {
                    mutableListOf(
                        "SATURDAY" to it,
                        "SUNDAY" to it,
                        "MONDAY" to it,
                        "TUESDAY" to it,
                        "WEDNESDAY" to it,
                        "THURSDAY" to it
                    )
                }
                TotalJson.generalList.teachersNames.forEach {
                    val teacherName = it.teacherName
                    it.openDays.forEach {
                        val day = it.dayName
                        val startTime = it.startTime
                        val endTime = it.endTime

                        val teacherLockedTime = allTimes.filterNot {
                            it.first == day && (it.second.start.toLocalTime().isAfter(
                                LocalTime.of(
                                    startTime.split(":")[0].toInt(),
                                    startTime.split(":")[1].toInt()
                                ).minusMinutes(1)
                            ) && (it.second.endInclusive.toLocalTime().isBefore(
                                LocalTime.of(
                                    endTime.split(":")[0].toInt(),
                                    endTime.split(":")[1].toInt()
                                ).plusMinutes(1)
                            )))
                        }.flatMap {
                            mutableListOf(teacherName to it)
                        }.toMutableList()
                        // occupiedTeacherBreaks.addAll(teacherLockedTime)
                        teacherLockedTime.forEach {
                            occupiedTeacherBreaks.add("${it.second.first}${it.second.second}${it.first}")
                        }

                    }
                }
            }
            courseSchedule.totalJson.generalList.courseGroups.forEach {
                it.presentedCourses.forEach {
                    motherCourses.add(
                        it
                    )
                }
            }
            motherCourses.forEach {
                // occupiedTeacherEntryBreaks.add(it.teacher to (it.groupYear.map { it.toString() }.toMutableList()))
                occupiedTeacherEntryBreaks.add("${it.teacher}${it.groupYear}")
            }
            isFirstTime = true
        }
    }

    override fun beforeEntityRemoved(entity: Any?) {

    }

    override fun afterVariableChanged(entity: Any, variableName: String) {
        val lecture = (entity as Lecture)

        if (lecture.roomNumber != null && lecture.entry != null && lecture.teacher != null && lecture.period != null && lecture.day != null) {
            when (variableName) {
                "day" -> {
                    if (occupiedRoomBreaks.contains("${lecture.day.first}${lecture.period.first}${lecture.roomNumber.first}")) {
                        hardScore -= 100
                        println("daysajjad")
                        return
                    }
                    if (occupiedTeacherBreaks.contains("${lecture.day.first}${lecture.period.first}${lecture.teacher.first}")) {
                        hardScore -= 100
                        println("daysajjad")
                        return
                    }
                    if (occupiedGroupBreaks.contains("${lecture.day.first}${lecture.period.first}${lecture.entry.first}")) {
                        hardScore -= 100
                        println("daysajjad")
                        return
                    }


                }
                "entry" -> {
                    if (occupiedGroupBreaks.contains("${lecture.day.first}${lecture.period.first}${lecture.entry.first}")) {
                        hardScore -= 100
                        println("entrysajjad")
                        return
                    }
                    if (!occupiedTeacherEntryBreaks.contains("${lecture.teacher.first}${lecture.entry.first}")) {
                        hardScore -= 100
                        println("entrysajjad")
                        return
                    }
                }
                "period" -> {
                    if (occupiedRoomBreaks.contains("${lecture.day.first}${lecture.period.first}${lecture.roomNumber.first}")) {
                        hardScore -= 100
                        println("daysajjad")
                        return
                    }
                    if (occupiedTeacherBreaks.contains("${lecture.day.first}${lecture.period.first}${lecture.teacher.first}")) {
                        hardScore -= 100
                        println("daysajjad")
                        return
                    }
                    if (occupiedGroupBreaks.contains("${lecture.day.first}${lecture.period.first}${lecture.entry.first}")) {
                        hardScore -= 100
                        println("daysajjad")
                        return
                    }


                }
                "teacher" -> {
                    if (occupiedTeacherBreaks.contains("${lecture.day.first}${lecture.period.first}${lecture.teacher.first}")) {
                        hardScore -= 100
                        println("daysajjad")
                        return
                    }
                    if (!occupiedTeacherEntryBreaks.contains("${lecture.teacher.first}${lecture.entry.first}")) {
                        hardScore -= 100
                        println("entrysajjad")
                        return
                    }


                }
                "room" -> {
                    if (occupiedRoomBreaks.contains("${lecture.day.first}${lecture.period.first}${lecture.roomNumber.first}")) {
                        hardScore -= 100
                        println("daysajjad")
                        return
                    }
                }
            }
            println("succeed")
            if (initializedCourses.contains("${lecture.day.first}${lecture.period.first}${lecture.roomNumber.first}${lecture.entry.first}${lecture.teacher.first}")) {
                hardScore -= 500
                return
            } // else {
            hardScore += 100
            println("added")
/*
                initializedCourses.add("${lecture.day.first}${lecture.period.first}${lecture.roomNumber.first}${lecture.entry.first}${lecture.teacher.first}")
*/
            occupiedGroupBreaks.add("${lecture.day.first}${lecture.period.first}${lecture.entry.first}")
            occupiedRoomBreaks.add("${lecture.day.first}${lecture.period.first}${lecture.roomNumber.first}")
            occupiedTeacherBreaks.add("${lecture.day.first}${lecture.period.first}${lecture.teacher.first}")
            occupiedTeacherEntryBreaks.remove("${lecture.teacher.first}${lecture.entry.first}")
            //  }
        }

    }

    override fun calculateScore(): Score<out Score<*>> {
        println(occupiedTeacherBreaks.toString() + "gholamreza")
        return HardSoftScore.valueOf(hardScore, softScore)
    }

    override fun beforeEntityAdded(entity: Any?) {

    }

    override fun afterEntityAdded(entity: Any?) {

    }

    override fun afterEntityRemoved(entity: Any?) {

    }

    override fun beforeVariableChanged(entity: Any, variableName: String) {

    }


    /*
    * checking teachers Times is filled or no
    * */
/*
    private fun checkPeriodConstraint(lecture: Lecture): Boolean {
        if (TimeIsWithinTeacherBreaks(lecture)) {
            hardScoreChange = -100
            return true
        }
        if (timeIsWithinGroupTimes(lecture)) {
            hardScoreChange = -100
            return true
        }
        if (TimeIsWithinRoomTimes(lecture)) {
            hardScoreChange = -100
            return true
        }
        return false
    }
*/

/*
    private fun checkDayConstraint(lecture: Lecture): Boolean {
        if (TimeIsWithinTeacherBreaks(lecture)) {
            hardScoreChange = -100
            return true
        }
        if (timeIsWithinGroupTimes(lecture)) {
            hardScoreChange = -100
            return true
        }
        if (TimeIsWithinRoomTimes(lecture)) {
            hardScoreChange = -100
            return true
        }
        return false
    }
*/

/*
    private fun checkEntryConstraints(lecture: Lecture): Boolean {
        if (doWeHaveThisEntryWithTeacher(lecture)) {
            hardScoreChange = -100
            return true
        }
        if (timeIsWithinGroupTimes(lecture)) {
            hardScoreChange = -100
            return true
        }
        return false
    }
*/

/*
    private fun checkTeacherConstraints(lecture: Lecture): Boolean {
        if (doWeHaveThisEntryWithTeacher(lecture)) {
            hardScoreChange = -100
            return true
        }
        if (TimeIsWithinTeacherBreaks(lecture)) {
            hardScoreChange = -100
            return true
        }
        return false
    }
*/

/*
    private fun checkRoomConstraint(lecture: Lecture): Boolean {
        if (TimeIsWithinRoomTimes(lecture)) {
            hardScoreChange = -100
            return true
        }
        return false
    }
*/

/*
    private fun TimeIsWithinTeacherBreaks(lecture: Lecture): Boolean {
        val timesList = splitTime(lecture.period.first)
        val lectureDay = lecture.day.first
        val teacherName = lecture.teacher.first
        val condition = (timesList.any {
            val lectureTime = it
            occupiedTeacherBreaks.any {
                it.second == lectureTime
                        &&
                        it.second.first == lectureDay
                        &&
                        it.first == teacherName
            }
        })
        if (condition)
            addPeriodTeacher(lecture)
        return condition
    }
*/

/*
    private fun timeIsWithinGroupTimes(lecture: Lecture): Boolean {

        val timesList = splitTime(lecture.period.first)
        val lectureDay = lecture.day.first
        val groupNames = lecture.entry.first.map { it.toString() }
        val condition = timesList.any {
            val lectureTime = it
            occupiedGroupBreaks.any {
                it.second == lectureTime
                        &&
                        it.first == groupNames
                        &&
                        lectureDay == it.second.first
            }
        }
        if (condition)
            addPeriodEntry(lecture)
        return condition
    }
*/

/*
    private fun doWeHaveThisEntryWithTeacher(lecture: Lecture): Boolean {
        var index = 0
        val condition = occupiedTeacherEntryBreaks.any {
            it.first == lecture.teacher.first &&
                    // it.second == lecture.entry.first.map { it.toString() }
                    it.second.all {
                        val result = (it[index].toString() == lecture.entry.first[index].toString())
                        index++
                        return result
                    }
        }
        if (condition)
            removeEntryTeacher(lecture)
        return !condition
    }
*/

/*
    private fun TimeIsWithinRoomTimes(lecture: Lecture): Boolean {
        val timesList = splitTime(lecture.period.first)
        val lectureDay = lecture.day.first
        val roomsNames = lecture.roomNumber.first
        val condition = timesList.any {
            val lectureTime = it
            occupiedRoomBreaks.any {
                it.second == lectureTime
                        &&
                        it.second.first == lectureDay
                        &&
                        it.first == roomsNames
            }
        }

        if (condition) {
            addPeriodClassRoom(lecture)
        }
        return condition
    }
*/


    private fun splitTime(time: ClosedRange<LocalDateTime>): MutableList<ClosedRange<LocalDateTime>> {
        return generateSequence(operatingDates.start.atTime(time.start.hour, time.start.minute)) { dt ->
            dt.plusMinutes(15).takeIf {
                it.plusMinutes(15) <= operatingDates.endInclusive.atTime(
                    time.endInclusive.hour,
                    time.endInclusive.minute
                )
            }
        }.map { it..it.plusMinutes(15) }.toMutableList()
    }


/*
    private fun addPeriodClassRoom(lecture: Lecture) {
        occupiedRoomBreaks.add(lecture.roomNumber.first.toString() to (lecture.day.first to lecture.period.first))
    }
*/

/*
    private fun addPeriodTeacher(lecture: Lecture) {
        occupiedTeacherBreaks.add(lecture.teacher.first to (lecture.day.first to lecture.period.first))
    }
*/

/*
    private fun addPeriodEntry(lecture: Lecture) {
        occupiedGroupBreaks.add(lecture.entry.first.map { it.toString() }.toMutableList() to (lecture.day.first to lecture.period.first))
    }
*/

/*
    private fun removeEntryTeacher(lecture: Lecture) {
        occupiedTeacherEntryBreaks.remove(lecture.teacher.first to lecture.entry.first.map { it.toString() })
    }
*/

}