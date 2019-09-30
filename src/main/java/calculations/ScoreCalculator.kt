package calculations

import org.optaplanner.core.api.score.Score
import org.optaplanner.core.api.score.buildin.hardsoft.HardSoftScore
import org.optaplanner.core.impl.score.director.easy.EasyScoreCalculator

class ScoreCalculator : EasyScoreCalculator<CourseSchedule> {

    override fun calculateScore(courseSchedule: CourseSchedule): Score<*> {
        val asssignedArray = hashSetOf<String>()
        var hardScore = -1
        var softScore = -1
        for ((level, lecture) in courseSchedule.lectureList.withIndex()) {
            if (lecture != null) {
                if (lecture.entry != null) {
                    if (lecture.teacher.second && lecture.entry.second && lecture.period.second && lecture.roomNumber.second && lecture.day.second) {
                        if (asssignedArray.contains("${lecture.teacher}${lecture.entry}${lecture.period}${lecture.roomNumber}${lecture.day}")) {
                            println("$hardScore sajjad")
                        } else {
                            asssignedArray.add("${lecture.teacher}${lecture.entry}${lecture.period}${lecture.roomNumber}${lecture.day}")
                            hardScore += 4
                            println("$hardScore sajjad")
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

}