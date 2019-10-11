package calculations

/*
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
                null to true,
                null to true
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

        */
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
          println(all.size)*//*

    }
}




*/
object ClassTest {
    @JvmStatic
    fun main(args: Array<String>) {

        val numbers = 1..10
        val myList = numbers.sorted()
        var totalValue = 0
         val max = myList.max()!!
        for (counter in myList) {
            totalValue += (findFactorial(max) / (findFactorial(max - counter) * findFactorial(counter))).toInt()
        }
        println(totalValue.toString())

    }

    fun findFactorial(num: Int): Long {
        if (num < 1) {
            println("Please provide non-negative number.")
        }
        var factorial: Long = 1
        for (i in num downTo 2) {
            factorial = factorial * i
        }
        return factorial
    }
}
/*
    fun main(args: Array<String>) {
    val numbers = 1..3
    val myList = numbers.sorted()
    val max = myList.max()
    for (counter in myList) {
        findFactorial(5)
    }
    fun findFactorial(num: Int): Long {
        if (num < 1) {
            println("Please provide non-negative number.")
        }
        var factorial: Long = 1
        for (i in num downTo 2) {
            factorial = factorial * i
        }
        return factorial
    }

}
*/
