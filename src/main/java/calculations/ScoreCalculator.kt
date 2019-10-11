package calculations

import com.example.CourseDataClass
import com.example.TeachersInfoDataClass
import org.optaplanner.core.api.score.Score
import org.optaplanner.core.api.score.buildin.hardsoft.HardSoftScore
import org.optaplanner.core.impl.score.director.easy.EasyScoreCalculator
import org.slf4j.LoggerFactory
import java.time.LocalDateTime
import java.time.LocalTime

class ScoreCalculator : EasyScoreCalculator<CourseSchedule> {
    val logger = LoggerFactory.getLogger("CourseSchedule")

    override fun calculateScore(courseSchedule: CourseSchedule): Score<*> {
        val asssignedArray = hashSetOf<String>()
        val teachersTimes = hashSetOf<String>()
        val classesTimes = hashSetOf<String>()
        val groupsTimes = hashSetOf<String>()
        val teacherBreaks = arrayListOf<Pair<String, LocalDateTime>>()
        val groupBreaks = arrayListOf<Pair<String, LocalDateTime>>()

        val motherCourses = arrayListOf<CourseDataClass>()
        courseSchedule.totalJson.generalList.courseGroups.forEach { it.presentedCourses.forEach { motherCourses.add(it) } }
        val openCourses = arrayListOf<Pair<ArrayList<Int>, String>>()
        motherCourses.forEach { openCourses.add(it.groupYear to it.teacher) }
        // println("$motherCourses  reza ")
        val openTimes = courseSchedule.totalJson.generalList.teachersNames
        var hardScore = -1
        var softScore = -1
        for ((level, lecture) in courseSchedule.lectureList.withIndex()) {
            if (lecture != null) {
                if (lecture.entry != null) {
                    if (lecture.teacher.second && lecture.entry.second && lecture.period.second && lecture.roomNumber.second && lecture.day.second) {
                        if (asssignedArray.contains("${lecture.teacher}${lecture.entry}${lecture.period}${lecture.roomNumber}${lecture.day}") || teachersTimes.contains(
                                "${lecture.teacher}${lecture.period}${lecture.day}"
                            ) || classesTimes.contains("${lecture.roomNumber}${lecture.period}${lecture.day}")
                            || lecture.entry.first.any { it.toString() == "${it}${lecture.period}${lecture.day}" }
                            || !openCourses.contains(lecture.entry.first to lecture.teacher.first)
                            || !isWithinRange(lecture, openTimes)
                            || teacherBreaks.any {
                                it.first == "${lecture.teacher}${lecture.day}" &&
                                        lecture.period.first.start == it.second
                            }
                            || groupBreaks.any {
                                it.first == "${lecture.entry}${lecture.day}" &&
                                        lecture.period.first.start == it.second
                            }
                        ) {
                            hardScore -= 4
                        } else {
                            openCourses.remove(
                                openCourses.first {
                                    it.first == lecture.entry.first && it.second == lecture.teacher.first
                                }
                            )
                            groupBreaks.add("${lecture.entry}${lecture.day}" to lecture.period.first.endInclusive)

                            groupBreaks.add(
                                "${lecture.entry}${lecture.day}" to lecture.period.first.endInclusive.plusMinutes(
                                    15
                                )
                            )
                            teacherBreaks.add("${lecture.teacher}${lecture.day}" to lecture.period.first.endInclusive)
                            teacherBreaks.add(
                                "${lecture.entry}${lecture.teacher}" to lecture.period.first.endInclusive.plusMinutes(
                                    15
                                )
                            )


                            teachersTimes.add("${lecture.teacher}${lecture.period}${lecture.day}")
                            classesTimes.add("${lecture.roomNumber}${lecture.period}${lecture.day}")

                            lecture.entry.first.forEach {

                                groupsTimes.add("${/*lecture.entry*/it}${lecture.period}${lecture.day}")


                            }

                            asssignedArray.add("${lecture.teacher}${lecture.entry}${lecture.period}${lecture.roomNumber}${lecture.day}")
                            hardScore += 4
                            if (lecture.period.first.start.minute == 0) {
                                softScore += 10
                            } else if (lecture.period.first.start.minute == 30) {
                                softScore += 5
                            }
                            // println("$hardScore sajjad")
                            /*  } else {
                                  hardScore -= 4
                              }*/
                        }
                    } else {
                        hardScore -= 4
                        // println("$hardScore sajjad")
                    }
                }
            }
        }
        return HardSoftScore.valueOf(hardScore, softScore)
    }

    private fun isWithinRange(lecture: Lecture?, openTimes: ArrayList<TeachersInfoDataClass>): Boolean {
        return openTimes.filter {
            it.teacherName == (lecture!!.teacher.first)
        }.any {
            it.openDays.any {
                it.dayName == lecture!!.day.first
                        &&
                        ((lecture.period.first.start.toLocalTime().isAfter(
                            LocalTime.of(
                                it.startTime.split(":")[0].toInt(),
                                it.startTime.split(":")[1].toInt()
                            ).minusMinutes(1)
                        )
                                /* ||
                                 (("${lecture.period.first.start.toLocalTime().hour}:${lecture.period.first.start.toLocalTime().minute}") == (it.startTime))*/)
                                &&
                                (
                                        lecture.period.first.endInclusive.toLocalTime().isBefore(
                                            LocalTime.of(
                                                it.endTime.split(":")[0].toInt(),
                                                it.endTime.split(":")[1].toInt()
                                            ).plusMinutes(1)
                                        )
                                        /*       ||
                                               ("${lecture.period.first.endInclusive.toLocalTime().hour}:${lecture.period.first.endInclusive.toLocalTime().minute}") == (it.endTime))*/
                                        ))
            }
        }
    }


}