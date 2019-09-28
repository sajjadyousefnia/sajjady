package calculations

import org.optaplanner.core.api.score.Score
import org.optaplanner.core.api.score.buildin.hardsoft.HardSoftScore
import org.optaplanner.core.impl.score.director.easy.EasyScoreCalculator

class ScoreCalculator : EasyScoreCalculator<CourseSchedule> {

    override fun calculateScore(courseSchedule: CourseSchedule): Score<*> {
        var hardScore = 0
        var softScore = 0
        for (lecture in courseSchedule.lectureList) {
            if (lecture != null) {
                if (lecture.entry != null) {
                    if (lecture.entry.second) {
                        hardScore++
                    }
                }
                if (lecture.period != null) {
                    if (lecture.period.second)
                        hardScore++
                }
                if (lecture.roomNumber != null) {
                    if (lecture.roomNumber.second)
                        hardScore++
                }
                if (lecture.teacher != null) {
                    if (lecture.teacher.second)
                        hardScore++
                }
            }
        }
        return HardSoftScore.valueOf(hardScore, softScore)

        // var hardScore = 0
        // var softScore = 0
        /*val occupiedRooms = HashSet<String>()
        for (lecture in courseSchedule.lectureList) {
            if (lecture.period != null && lecture.roomNumber != null) {
                val roomInUse = lecture.period!!.toString() + ":" + lecture.roomNumber!!.toString()
                if (occupiedRooms.contains(roomInUse)) {
                    hardScore += -1
                } else {
                    occupiedRooms.add(roomInUse)
                }
            } else {
                hardScore += -1
            }
        }*/
        // return HardSoftScore.valueOf(hardScore, softScore)

    }
}