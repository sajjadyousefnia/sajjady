package calculations

import org.optaplanner.core.api.solver.SolverFactory

object ClassTest {

    // private val solver: Solver<CourseSchedule>? = null
    private var unsolvedCourseSchedule: CourseSchedule? = null

    @JvmStatic
    fun main(args: Array<String>) {
        unsolvedCourseSchedule = CourseSchedule()

        for (i in 0..9) {
            unsolvedCourseSchedule!!.lectureList.add(Lecture())
        }

        unsolvedCourseSchedule!!.entriesList.addAll(setOf(0 to true, 1 to false, 2 to true))
        unsolvedCourseSchedule!!.teachersList.addAll(setOf(0 to true, 1 to false, 2 to true, 3 to true, 4 to false))
        unsolvedCourseSchedule!!.roomList.addAll(
            setOf(
                0 to true,
                1 to false,
                2 to true,
                3 to true,
                4 to false,
                5 to true,
                6 to true
            )
        )
        unsolvedCourseSchedule!!.periodList.addAll(
            setOf(
                0 to true,
                1 to false,
                2 to true,
                3 to true,
                4 to false,
                5 to true,
                6 to true,
                7 to true,
                8 to false
            )
        )
        val solverFactory = SolverFactory.createFromXmlResource<CourseSchedule>("courseScheduleSolverConfiguration.xml")
        val solver = solverFactory.buildSolver()
        val solvedCourseSchedule = solver.solve(unsolvedCourseSchedule)
        solvedCourseSchedule.lectureList.forEach { println("${it.teacher} ${it.roomNumber} ${it.period} ${it.entry}") }
        // solvedCourseSchedule.printCourseSchedule()
        // Assert.assertNotNull(solvedCourseSchedule.getScore());
        // Assert.assertEquals(-4, solvedCourseSchedule.getScore().getHardScore());

    }
}
