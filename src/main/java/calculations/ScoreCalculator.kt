package calculations

import com.example.CourseDataClass
import com.soywiz.klock.*
import org.optaplanner.core.api.score.Score
import org.optaplanner.core.api.score.buildin.hardsoft.HardSoftScore
import org.optaplanner.core.impl.score.director.easy.EasyScoreCalculator

class ScoreCalculator : EasyScoreCalculator<CourseSchedule> {
    //   val logger = LoggerFactory.getLogger("CourseSchedule")

    override fun calculateScore(courseSchedule: CourseSchedule): Score<*> {
        val classesFreeTimes = arrayListOf<Pair<Int, MutableDayClockPair>>()
        val teachersFreeTimes = arrayListOf<Pair<String, MutableDayClockPair>>()
        val groupOpenTimes = arrayListOf<Pair<Int, MutableDayClockPair>>()
        val openCourses = arrayListOf<Pair<ArrayList<Int>, String>>()
        // val selectedTeachersTimes = arrayListOf<Pair<String, MutableDayClockPair>>()
        val selectedGroupOpenTimes = arrayListOf<Pair<Int, MutableDayClockPair>>()


        val days = arrayListOf<String>("SATURDAY", "SUNDAY", "MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY")
        for (counter in 1..courseSchedule.totalJson.generalList.classes.size) {
            days.forEach {
                classesFreeTimes.add(
                    counter to MutableDayClockPair(
                        it, (DateTimeRangeSet(
                            DateTimeRange(
                                DateTime(2020, 1, 1, 8, 0),
                                DateTime(2020, 1, 1, 18, 30)
                            )
                        ))
                    )
                )
            }
        }

        courseSchedule.totalJson.generalList.entriesYears.forEach {
            val year = it
            days.forEach {
                groupOpenTimes.add(
                    year to MutableDayClockPair(
                        it, DateTimeRangeSet(
                            DateTimeRange(
                                DateTime(2020, 1, 1, 8, 0),
                                DateTime(2020, 1, 1, 18, 30)
                            )
                        )
                    )
                )
            }
        }


        courseSchedule.totalJson.generalList.courseGroups.forEach {
            it.presentedCourses.forEach {
                for (counter in 0 until it.recurrences) {
                    val courseClass = CourseDataClass(
                        teacher = it.teacher,
                        courseName = it.courseName.toString() + "###" + counter.toString(),
                        recurrences = 1,
                        id = it.id, courseType = "theory",
                        groupYear = it.groupYear,
                        units = it.units
                    )
                    openCourses.add(courseClass.groupYear to courseClass.teacher)
                }
            }
        }

        (openCourses.forEach {
            println("${it.first}${it.second}")
        })



        courseSchedule.totalJson.generalList.teachersNames.forEach {
            val teacherName = it.teacherName
            it.openDays.forEach {
                teachersFreeTimes.add(
                    teacherName to MutableDayClockPair(
                        it.dayName, DateTimeRangeSet(
                            DateTimeRange(
                                DateTime(
                                    2020, 1, 1, it.startTime.split(":")[0].toInt(),
                                    it.startTime.split(":")[1].toInt()
                                ), DateTime(
                                    2020, 1, 1, it.endTime.split(":")[0].toInt(),
                                    it.endTime.split(":")[1].toInt()
                                )
                            )
                        )
                    )
                )
            }
        }
        (teachersFreeTimes.forEach {
            println("${it.first}${it.second.first}${it.second.second}")
        })
        var hardScore = -1
        var softScore = -1
        for ((level, lecture) in courseSchedule.lectureList.withIndex()) {
            if (lecture != null) {
                if (lecture.entry != null) {
                    if (lecture.teacher.second
                        && lecture.entry.second
                        && lecture.period.second
                        && lecture.roomNumber.second
                        && lecture.day.second
                    ) {
                        if (lecture.teacher.first != null && lecture.entry.first != null && lecture.period.first != null && lecture.roomNumber.first != null &&
                            lecture.day.first != null
                        ) {

                            if (teachersTimeIsOK(lecture, teachersFreeTimes) &&
                                classesTimesIsOk(lecture, classesFreeTimes)
                                && entriesTimesIsOK(lecture, groupOpenTimes) &&
                                openCoursesIsOK(lecture, openCourses)
                            // && checkIsNotBeforeAnyClass(lecture, groupOpenTimes, teachersFreeTimes)

                            ) {
                                openCourses.remove(
                                    openCourses.first {
                                        it.first == lecture.entry.first &&
                                                it.second == lecture.teacher.first
                                    })
                                val currentTime = DateTimeRangeSet(
                                    DateTimeRange
                                        (
                                        DateTime(
                                            2020,
                                            1,
                                            1,
                                            lecture.period.first.start.hour,
                                            lecture.period.first.start.minute
                                        ),
                                        DateTime(
                                            2020,
                                            1,
                                            1,
                                            lecture.period.first.endInclusive.hour,
                                            lecture.period.first.endInclusive.minute
                                        )/*.plus(TimeSpan(30.minutes.milliseconds))*/
                                    )
                                )
                                val timeToDeleteUntilHour = DateTimeRangeSet(
                                    DateTimeRange
                                        (
                                        DateTime(
                                            2020,
                                            1,
                                            1,
                                            lecture.period.first.endInclusive.hour,
                                            lecture.period.first.endInclusive.minute
                                        )/*.plus(TimeSpan(30.minutes.milliseconds))*/,
                                        DateTime(
                                            2020,
                                            1,
                                            1,
                                            lecture.period.first.endInclusive.hour,
                                            lecture.period.first.endInclusive.minute
                                        ).plus(TimeSpan(30.minutes.milliseconds))
                                    )
                                )



                                teachersFreeTimes.first {
                                    it.first == lecture.teacher.first &&
                                            it.second.first == lecture.day.first
                                }.second.second -= (currentTime + timeToDeleteUntilHour)


                                classesFreeTimes.first {
                                    it.first.toString() == lecture.roomNumber.first
                                            &&
                                            it.second.first == lecture.day.first
                                }.second.second -= currentTime


                                lecture.entry.first.forEach {
                                    val year = it
                                    groupOpenTimes.first {
                                        it.first == year
                                                &&
                                                it.second.first == lecture.day.first
                                    }.second.second -= (currentTime + timeToDeleteUntilHour)

                                }


/*
                                selectedTeachersTimes.add(
                                    lecture.teacher.first to
                                            MutableDayClockPair(
                                                first = lecture.day.first, second = DateTimeRangeSet(
                                                    DateTimeRange(
                                                        DateTime(
                                                            2020,
                                                            1,
                                                            1,
                                                            lecture.period.first.start.hour,
                                                            lecture.period.first.start.minute
                                                        ), DateTime(
                                                            2020,
                                                            1,
                                                            1,
                                                            lecture.period.first.endInclusive.hour,
                                                            lecture.period.first.endInclusive.minute
                                                        ).plus(TimeSpan(30.minutes.milliseconds))
                                                    )
                                                )
                                            )
                                )
*/
                                lecture.entry.first.forEach {
                                    selectedGroupOpenTimes.add(
                                        it to MutableDayClockPair(
                                            first = lecture.day.first, second = DateTimeRangeSet(
                                                DateTimeRange(
                                                    DateTime(
                                                        2020,
                                                        1,
                                                        1,
                                                        lecture.period.first.start.hour,
                                                        lecture.period.first.start.minute
                                                    ), DateTime(
                                                        2020,
                                                        1,
                                                        1,
                                                        lecture.period.first.endInclusive.hour,
                                                        lecture.period.first.endInclusive.minute
                                                    ).plus(TimeSpan(30.minutes.milliseconds))
                                                )
                                            )
                                        )
                                    )
                                }
                                hardScore += 5
                            } else {
                                hardScore -= 1000
                            }
                        } else {
                            hardScore -= 1000
                        }


                    } else {
                        hardScore -= 1000
                    }
                } else {
                    hardScore -= 1000
                }
            } else {
                hardScore -= 1000
            }
        }
        return HardSoftScore.valueOf(hardScore, softScore)
    }

    private fun checkIsNotBeforeAnyClass(
        lecture: Lecture,
        groupOpenTimes: java.util.ArrayList<Pair<Int, MutableDayClockPair>>,
        teachersFreeTimes: java.util.ArrayList<Pair<String, MutableDayClockPair>>
    ): Boolean {

        val entries = lecture.entry.first.all {
            val year = it
            return groupOpenTimes.any {
                year == it.first
                        &&
                        it.second.first == lecture.day.first
                        &&
                        it.second.second.intersection(
                            DateTimeRange(
                                DateTime(
                                    2020, 1, 1,
                                    lecture.period.first.start.hour,
                                    lecture.period.first.start.minute
                                ),
                                DateTime(
                                    2020,
                                    1,
                                    1,
                                    lecture.period.first.start.hour,
                                    lecture.period.first.start.minute
                                ).plus((TimeSpan(30.minutes.milliseconds)))
                            )
                        ).toString() == "[]"
            }
        }


        val teachers = teachersFreeTimes.any {
            lecture.teacher.first == it.first
                    &&
                    it.second.first == lecture.day.first
                    &&
                    it.second.second.intersection(
                        DateTimeRange(
                            DateTime(
                                2020, 1, 1,
                                lecture.period.first.start.hour,
                                lecture.period.first.start.minute
                            ),
                            DateTime(
                                2020,
                                1,
                                1,
                                lecture.period.first.start.hour,
                                lecture.period.first.start.minute
                            ).plus((TimeSpan(30.minutes.milliseconds)))
                        )
                    ).toString() == "[]"
        }

        println("teachersandentries are $teachers$entries")
        return !(teachers && entries)

    }


    private fun openCoursesIsOK(
        lecture: Lecture,
        openCourses: ArrayList<Pair<ArrayList<Int>, String>>
    ): Boolean {
        val courses = openCourses.any {
            it.second == lecture.teacher.first &&
                    it.first == lecture.entry.first
        }
        println("courses is $courses")
        return courses

    }

    private fun entriesTimesIsOK(
        lecture: Lecture,
        groupOpenTimes: ArrayList<Pair<Int, MutableDayClockPair>>
    ): Boolean {
        val entries = lecture.entry.first.all {
            val year = it
            return groupOpenTimes.any {
                year == it.first
                        &&
                        it.second.first == lecture.day.first
                        &&
                        it.second.second.contains(
                            DateTimeRange(
                                DateTime(
                                    2020, 1, 1,
                                    lecture.period.first.start.hour,
                                    lecture.period.first.start.minute
                                ),
                                DateTime(
                                    2020,
                                    1,
                                    1,
                                    lecture.period.first.endInclusive.hour,
                                    lecture.period.first.endInclusive.minute
                                )
                            )
                        )
            }


        }
        println("entriesBoolean is $entries")
        return entries
    }

    private fun classesTimesIsOk(
        lecture: Lecture,
        classesFreeTimes: ArrayList<Pair<Int, MutableDayClockPair>>
    ): Boolean {
        val classBoolean = classesFreeTimes.any {
            it.first.toString() == lecture.roomNumber.first
                    &&
                    it.second.first == lecture.day.first
                    &&
                    it.second.second.contains(
                        DateTimeRange(
                            DateTime(
                                2020, 1, 1,
                                lecture.period.first.start.hour,
                                lecture.period.first.start.minute
                            ),
                            DateTime(
                                2020,
                                1,
                                1,
                                lecture.period.first.endInclusive.hour,
                                lecture.period.first.endInclusive.minute
                            )
                        )
                    )
        }
        println("classBoolean is $classBoolean")
        return classBoolean
    }

    private fun teachersTimeIsOK(
        lecture: Lecture,
        teachersFreeTimes: ArrayList<Pair<String, MutableDayClockPair>>
    ): Boolean {
        println("entrance1${lecture.teacher} ${lecture.entry} ${lecture.period} ${lecture.roomNumber} ${lecture.day}")
        println("entrance2$teachersFreeTimes")
        val teacherBoolean = teachersFreeTimes.any {
            it.first == lecture.teacher.first
                    &&
                    it.second.first == lecture.day.first
                    &&
                    it.second.second.contains(
                        DateTimeRange(
                            DateTime(
                                2020, 1, 1,
                                lecture.period.first.start.hour,
                                lecture.period.first.start.minute
                            ),
                            DateTime(
                                2020,
                                1,
                                1,
                                lecture.period.first.endInclusive.hour,
                                lecture.period.first.endInclusive.minute
                            )
                        )
                    )
        }
        println("teacherBoolean is $teacherBoolean")
        return teacherBoolean
    }

}