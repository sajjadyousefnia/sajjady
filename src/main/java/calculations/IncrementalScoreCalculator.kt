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
    var hardScoreChange = 0
    var softScoreChange = 0
    lateinit var TotalJson: FirstClass
    val occupiedTeacherBreaks = mutableListOf<Pair<String, Pair<String, ClosedRange<LocalDateTime>>>>()
    val occupiedGroupBreaks = mutableListOf<Pair<MutableList<String>, Pair<String, ClosedRange<LocalDateTime>>>>()
    val occupiedTeacherEntryBreaks = mutableListOf<Pair<String, MutableList<String>>>()
    val occupiedRoomBreaks = mutableListOf<Pair<String, Pair<String, ClosedRange<LocalDateTime>>>>()

    val motherCourses = arrayListOf<CourseDataClass>()

    override fun resetWorkingSolution(courseSchedule: CourseSchedule) {
        TotalJson = courseSchedule.totalJson
        occupiedTeacherBreaks.clear()
        occupiedGroupBreaks.clear()
        motherCourses.clear()

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

                    val teacherLockedTime = allTimes.filter {
                        it.first == day && !(it.second.start.toLocalTime().isAfter(
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
                    occupiedTeacherBreaks.addAll(teacherLockedTime)
                }
            }
        }
        courseSchedule.totalJson.generalList.courseGroups.forEach { it.presentedCourses.forEach { motherCourses.add(it) } }
        motherCourses.forEach {
            occupiedTeacherEntryBreaks.add(it.teacher to (it.groupYear.map { it.toString() }.toMutableList()))
        }

    }

    override fun beforeEntityRemoved(entity: Any?) {

    }

    override fun afterVariableChanged(variable: Any, variableName: String) {
        hardScore += hardScoreChange
        hardScoreChange = 0

    }

    override fun calculateScore(): Score<out Score<*>> {
        return HardSoftScore.valueOf(hardScore, softScore)
    }

    override fun beforeEntityAdded(entity: Any?) {

    }

    override fun afterEntityAdded(entity: Any?) {

    }

    override fun afterEntityRemoved(entity: Any?) {

    }

    override fun beforeVariableChanged(entity: Any, variableName: String) {
        val lecture = (entity as Lecture)

        if (lecture.roomNumber != null && lecture.entry != null && lecture.teacher != null && lecture.period != null && lecture.day != null) {
            when (variableName) {
                "day" -> {
                    if (applyDayConstraint(lecture))
                        hardScoreChange = -100
                    return
                }
                "entry" -> {
                    if (applyEntryConstraints(lecture))
                        hardScoreChange = -100
                    return
                }
                "period" -> {
                    if (applyPeriodConstraint(lecture))
                        hardScoreChange = -100
                    return
                }
                "teacher" -> {
                    if (applyTeacherConstraints(lecture))
                        hardScoreChange = -100
                    return
                }
                "room" -> {
                    if (applyRoomConstraint(lecture))
                        hardScoreChange = -100
                    return
                }
            }
            hardScoreChange = 100
            occupiedGroupBreaks.add((lecture.entry.first.map { it.toString() }.toMutableList() to (lecture.day.first to lecture.period.first)))
            occupiedRoomBreaks.add(lecture.roomNumber.first.toString() to (lecture.day.first to lecture.period.first))
            occupiedTeacherBreaks.add(lecture.teacher.first to (lecture.day.first to lecture.period.first))
            occupiedTeacherEntryBreaks.add(lecture.teacher.first to lecture.entry.first.map { it.toString() }.toMutableList())
        }
    }

    /*
    * checking teachers Times is filled or no
    * */

    private fun applyPeriodConstraint(lecture: Lecture): Boolean {
        if (TimeIsWithinTeacherBreaks(lecture) && lecture.teacher.first != null) {
            hardScoreChange = -100
            return true
        }
        if (TimeIsWithinGroupTimes(lecture) && lecture.entry.first != null) {
            hardScoreChange = -100
            return true
        }
        if (TimeIsWithinRoomTimes(lecture) && lecture.roomNumber.first != null) {
            hardScoreChange = -100
            return true
        }
        return false
    }

    private fun applyDayConstraint(lecture: Lecture): Boolean {
        if (TimeIsWithinTeacherBreaks(lecture) && lecture.teacher.first != null) {
            hardScoreChange = -100
            return true
        }
        if (TimeIsWithinGroupTimes(lecture) && lecture.entry.first != null) {
            hardScoreChange = -100
            return true
        }
        if (TimeIsWithinRoomTimes(lecture) && lecture.roomNumber.first != null) {
            hardScoreChange = -100
            return true
        }
        return false
    }

    private fun applyEntryConstraints(lecture: Lecture): Boolean {
        if (doWeHaveThisEntryWithTeacher(lecture)) {
            hardScoreChange = -100
            return true
        }
        if (TimeIsWithinGroupTimes(lecture) && lecture.entry.first != null) {
            hardScoreChange = -100
            return true
        }
        return false
    }

    private fun applyTeacherConstraints(lecture: Lecture): Boolean {
        if (doWeHaveThisEntryWithTeacher(lecture)) {
            hardScoreChange = -100
            return true
        }
        if (TimeIsWithinTeacherBreaks(lecture) && lecture.teacher.first != null) {
            hardScoreChange = -100
            return true
        }
        return false
    }

    private fun applyRoomConstraint(lecture: Lecture): Boolean {
        if (TimeIsWithinRoomTimes(lecture) && lecture.roomNumber.first != null) {
            hardScoreChange = -100
            return true
        }
        return false
    }

    private fun TimeIsWithinTeacherBreaks(lecture: Lecture): Boolean {
        val timesList = splitTime(lecture.period.first)
        val lectureDay = lecture.day.first
        val teacherName = lecture.teacher.first
        return timesList.any {
            val lectureTime = it
            occupiedTeacherBreaks.any {
                it.second == lectureTime
                        &&
                        it.second.first == lectureDay
                        &&
                        it.first == teacherName
            }
        }
    }

    private fun TimeIsWithinGroupTimes(lecture: Lecture): Boolean {

        val timesList = splitTime(lecture.period.first)
        val lectureDay = lecture.day.first
        val groupNames = lecture.entry.first.map { it.toString() }
        return timesList.any {
            val lectureTime = it
            occupiedGroupBreaks.any {
                it.second == lectureTime
                        &&
                        it.first == groupNames
                        &&
                        lectureDay == it.second.first
            }


        }


    }

    private fun doWeHaveThisEntryWithTeacher(lecture: Lecture): Boolean {
        return occupiedTeacherEntryBreaks.any {
            it.first == lecture.teacher.first &&
                    it.second.flatMap { mutableListOf(it.toInt()) } == lecture.entry.first

        }

    }


    private fun TimeIsWithinRoomTimes(lecture: Lecture): Boolean {
        val timesList = splitTime(lecture.period.first)
        val lectureDay = lecture.day.first
        val roomsNames = lecture.roomNumber.first
        return timesList.any {
            val lectureTime = it
            occupiedRoomBreaks.any {
                it.second == lectureTime
                        &&
                        it.second.first == lectureDay
                        &&
                        it.first == roomsNames
            }
        }
    }


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

    private fun isFirstIntervalWithinSecondInterval(
        first: ClosedRange<LocalDateTime>,
        second: ClosedRange<LocalDateTime>
    ): Boolean {
        return (first.start.isAfter(second.start.minusMinutes(1))
                &&
                first.endInclusive.isBefore(second.endInclusive.plusMinutes(1)))
    }
}