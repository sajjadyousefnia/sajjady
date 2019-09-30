package calculations

import org.optaplanner.core.api.solver.SolverFactory

object ClassTest {

    // private val solver: Solver<CourseSchedule>? = null
    private var unsolvedCourseSchedule: CourseSchedule? = null

    @JvmStatic
    fun main(args: Array<String>) {
        unsolvedCourseSchedule = CourseSchedule()

        for (i in 0..20) {
            unsolvedCourseSchedule!!.lectureList.add(Lecture())
        }
        unsolvedCourseSchedule!!.daysList.addAll(setOf("sunday" to true, "monday" to true))
        unsolvedCourseSchedule!!.entriesList.addAll(setOf("93" to true, "94" to true))
        unsolvedCourseSchedule!!.teachersList.addAll(
            setOf(
                "zahmatkesh" to true,
                "farughi" to true

            )
        )
        unsolvedCourseSchedule!!.roomList.addAll(
            setOf(
                "0" to true,
                "1" to true

            )
        )
        unsolvedCourseSchedule!!.periodList.addAll(
            setOf(
                "1 until 2.5" to true,
                "5 until 6.5" to true
            )
        )
        val x = (unsolvedCourseSchedule!!)
        val solverFactory = SolverFactory.createFromXmlResource<CourseSchedule>("courseScheduleSolverConfiguration.xml")

        val solver = solverFactory.buildSolver()

        val solvedCourseSchedule = solver.solve(unsolvedCourseSchedule)
        solvedCourseSchedule.lectureList.forEach { println("${it.teacher} ${it.roomNumber}  ${it.period} ${it.entry} ${it.day}") }
        // x.forEach { println(it.toString()) }
        println(x)
//        val x = (LocalTime.of(10, 16)..LocalTime.of(2017, 10, 20))

        /*  val all = generateSequence(operatingDates.start.atStartOfDay()) { dt ->
              dt.plusMinutes(90).takeIf { it.plusMinutes(90) <= operatingDates.endInclusive.atTime(18, 31) }
          }.map { it..it.plusMinutes(90) }.toMutableList()
          // all.forEach { println("${it}\n") }
          val tempALl = mutableListOf<ClosedRange<LocalDateTime>>()
          for (counter in 1..5) {
              val newAll = mutableListOf<ClosedRange<LocalDateTime>>()

              all.filter {
                  LocalTime.of(it.endInclusive.hour, it.endInclusive.minute).plusMinutes((15 * counter).toLong())
                      .isBefore(LocalTime.of(18, 31))
              }.forEach {
                  val startValue = it.start.plusMinutes((15 * counter).toLong())
                  val endValue = it.endInclusive.plusMinutes((15 * counter).toLong())
                  newAll.add(startValue..endValue)
              }
              tempALl.addAll(newAll)
          }
          all.addAll(tempALl)
          // all.forEach { print("${it}\n") }
          println(all.size)*/
    }
}
