package calculations

import com.example.CourseDataClass
import com.example.TeachersInfoDataClass
import org.optaplanner.core.api.score.Score
import org.optaplanner.core.api.score.buildin.hardsoft.HardSoftScore
import org.optaplanner.core.impl.score.director.easy.EasyScoreCalculator
import org.slf4j.LoggerFactory
import java.time.LocalTime

class ScoreCalculator : EasyScoreCalculator<CourseSchedule> {
    val logger = LoggerFactory.getLogger("CourseSchedule")

    override fun calculateScore(courseSchedule: CourseSchedule): Score<*> {
        val asssignedArray = hashSetOf<String>()
        val teachersTimes = hashSetOf<String>()
        val classesTimes = hashSetOf<String>()
        val groupsTimes = hashSetOf<String>()
        val motherCourses = arrayListOf<CourseDataClass>()
        courseSchedule.totalJson.generalList.courseGroups.forEach { it.presentedCourses.forEach { motherCourses.add(it) } }
        val openCourses = arrayListOf<Pair<String, String>>()
        motherCourses.forEach { openCourses.add(it.groupYear.toString() to it.teacher) }
        println("$motherCourses  reza ")
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
                            || groupsTimes.contains("${lecture.entry}${lecture.period}${lecture.day}")
                            || !openCourses.contains(lecture.entry.first to lecture.teacher.first)
                         || !isWithinRange(lecture, openTimes)
                        ) {
                            hardScore -= 4
                        } else {
                            // println("${lecture.entry}${lecture.teacher}${lecture.day}${lecture.period}${lecture.roomNumber} reza ${hardScore + 4}")
                            // println("$openCourses  reza  ")
                            //    if (isWithinRange(lecture)) {
                            openCourses.remove(
                                openCourses.first {
                                    it.first == lecture.entry.first && it.second == lecture.teacher.first
                                }
                            )
                            teachersTimes.add("${lecture.teacher}${lecture.period}${lecture.day}")
                            classesTimes.add("${lecture.roomNumber}${lecture.period}${lecture.day}")
                            groupsTimes.add("${lecture.entry}${lecture.period}${lecture.day}")
                            asssignedArray.add("${lecture.teacher}${lecture.entry}${lecture.period}${lecture.roomNumber}${lecture.day}")
                            hardScore += 4
                            println("$hardScore sajjad")
                            /*  } else {
                                  hardScore -= 4
                              }*/
                        }
                    } else {
                        hardScore -= 4
                        println("$hardScore sajjad")
                    }
                }
            }
        }
        return HardSoftScore.valueOf(hardScore, softScore)
    }

    private fun isWithinRange(lecture: Lecture?, openTimes: ArrayList<TeachersInfoDataClass>): Boolean {
        return openTimes!!.filter {
            it.teacherName == (lecture!!.teacher.first)
        }.any {
            it.openDays.any {
                it.dayName == lecture!!.day.first
                        &&
                        ((lecture.period.first.start.toLocalTime().isAfter(
                            LocalTime.of(
                                it.startTime.split(":")[0].toInt(),
                                it.startTime.split(":")[1].toInt()
                            )
                        )
                                ||
                                (("${lecture.period.first.start.toLocalTime().hour}:${lecture.period.first.start.toLocalTime().minute}") == (it.startTime)))
                                &&
                                (
                                        lecture.period.first.endInclusive.toLocalTime().isBefore(
                                            LocalTime.of(
                                                it.endTime.split(":")[0].toInt(),
                                                it.endTime.split(":")[1].toInt()
                                            )
                                        )
                                                ||
                                                ("${lecture.period.first.endInclusive.toLocalTime().hour}:${lecture.period.first.endInclusive.toLocalTime().minute}") == (it.endTime))
                                )
            }
        }
    }


}