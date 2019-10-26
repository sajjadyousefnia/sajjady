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
        val assignedTotalCourses = hashSetOf<String>()

        val teachersFreeTimes = hashSetOf<Pair<String, Pair<String, ClosedRange<LocalTime>>>>()
        val classesFilledTimes = hashSetOf<Pair<String, Pair<String, LocalDateTime>>>()

        // val groupsTimes = hashSetOf<String>()
        val teacherBreaks = arrayListOf<Pair<String, LocalDateTime>>()
        val groupBreaks = arrayListOf<Pair<String, LocalDateTime>>()
        val openCourses = arrayListOf<Pair<ArrayList<Int>, String>>()



        courseSchedule.totalJson.generalList.courseGroups.forEach {
            it.presentedCourses.forEach {
                for (counter in 0 until it.recurrences) {
                    val courseClass = CourseDataClass(
                        teacher = it.teacher,
                        courseName = it.courseName.toString() + "###" + counter.toString(),
                        recurrences = 1,
                        id = it.id, courseType = "theory",
                        groupYear = it.groupYear, units = it.units
                    )
                    openCourses.add(courseClass.groupYear to courseClass.teacher)
                }
            }
        }

        /* motherCourses.forEach {
             val recuurence = it.recurrences
             openCourses.add(it.groupYear to it.teacher)
         }*/
        /*motherCourses.forEach {
            openCourses.add(it.groupYear to it.teacher)
        }*/

        // println("$motherCourses  reza ")
        courseSchedule.totalJson.generalList.teachersNames.forEach {
            val teacherName = it.teacherName
            it.openDays.forEach {
                teachersFreeTimes.add(
                    teacherName to (it.dayName to LocalTime.of(
                        it.startTime.split(":")[0].toInt(),
                        it.startTime.split(":")[1].toInt()
                    )..LocalTime.of(
                        it.endTime.split(":")[0].toInt(),
                        it.endTime.split(":")[1].toInt()
                    ))
                )
            }
        }


        var hardScore = -1
        var softScore = -1
        for ((level, lecture) in courseSchedule.lectureList.withIndex()) {
            if (lecture != null) {
                if (lecture.entry != null) {
                    if (lecture.teacher.second && lecture.entry.second && lecture.period.second && lecture.roomNumber.second && lecture.day.second) {
                        if (assignedTotalCourses.contains("${lecture.teacher}${lecture.entry}${lecture.period}${lecture.roomNumber}${lecture.day}") /*|| teachersFreeTimes.contains(
                                "${lecture.teacher}${lecture.period}${lecture.day}"
                            ) *//*|| classesFilledTimes.contains("${lecture.roomNumber}${lecture.period}${lecture.day}")*/
                            || lecture.entry.first.any { it.toString() == "${it}${lecture.period}${lecture.day}" }
                            || !openCourses.contains(lecture.entry.first to lecture.teacher.first)
                            /*|| !isWithinRange(lecture, openTimes)*/
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



                            lecture.entry.first.forEach {

                                //   groupsTimes.add("${/*lecture.entry*/it}${lecture.period}${lecture.day}")


                            }

                            assignedTotalCourses.add("${lecture.teacher}${lecture.entry}${lecture.period}${lecture.roomNumber}${lecture.day}")
                            hardScore += 4
                            if (lecture.period.first.start.minute == 0) {
                                softScore += 10
                            } else if (lecture.period.first.start.minute == 30) {
                                softScore += 5
                            }
                            val nextTime =
                                lecture.period.first.start.plusMinutes(120)..lecture.period.first.endInclusive.plusMinutes(
                                    120
                                )
                            val beforeTime =
                                lecture.period.first.start.minusMinutes(120)..lecture.period.first.endInclusive.minusMinutes(
                                    120
                                )
                            if (assignedTotalCourses.contains("${lecture.teacher}${lecture.entry}${nextTime to true}${lecture.roomNumber}${lecture.day}")) {
                                softScore += 100
                            }

                            if (assignedTotalCourses.contains("${lecture.teacher}${lecture.entry}${beforeTime to true}${lecture.roomNumber}${lecture.day}")) {
                                softScore += 100
                            }
                            // println("$hardScore sajjad")
                            /*  } else {
                                  hardScore -= 4
                              }*/
                        }

                        teachersFreeTimes.add("${lecture.teacher}${lecture.period}${lecture.day}")
                        classesFilledTimes.add("${lecture.roomNumber}${lecture.period}${lecture.day}")


                    } else {
                        hardScore -= 1000
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