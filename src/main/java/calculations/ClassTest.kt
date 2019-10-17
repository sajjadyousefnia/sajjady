package calculations

import com.example.operatingDates

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

        val json = "{\n" +
                "  \"requested\": {\n" +
                "    \"workingTime\": {\n" +
                "      \"startTime\": \"8:00\",\n" +
                "      \"endTime\": \"18:00\"\n" +
                "    },\n" +
                "    \"teachersNames\": [\n" +
                "      {\n" +
                "        \"teacherName\": \"farughi\",\n" +
                "        \"openDays\": [\n" +
                "          {\n" +
                "            \"dayName\": \"SATURDAY\",\n" +
                "            \"startTime\": \"08:00\",\n" +
                "            \"endTime\": \"12:00\"\n" +
                "          },\n" +
                "          {\n" +
                "            \"dayName\": \"SUNDAY\",\n" +
                "            \"startTime\": \"08:00\",\n" +
                "            \"endTime\": \"12:00\"\n" +
                "          },\n" +
                "          {\n" +
                "            \"dayName\": \"MONDAY\",\n" +
                "            \"startTime\": \"08:00\",\n" +
                "            \"endTime\": \"12:00\"\n" +
                "          }\n" +
                "        ]\n" +
                "      },\n" +
                "      {\n" +
                "        \"teacherName\": \"zahmatkesh\",\n" +
                "        \"openDays\": [\n" +
                "          {\n" +
                "            \"dayName\": \"TUESDAY\",\n" +
                "            \"startTime\": \"08:00\",\n" +
                "            \"endTime\": \"12:00\"\n" +
                "          },\n" +
                "          {\n" +
                "            \"dayName\": \"WEDNESDAY\",\n" +
                "            \"startTime\": \"08:00\",\n" +
                "            \"endTime\": \"12:00\"\n" +
                "          },\n" +
                "          {\n" +
                "            \"dayName\": \"THURSDAY\",\n" +
                "            \"startTime\": \"08:00\",\n" +
                "            \"endTime\": \"12:30\"\n" +
                "          }\n" +
                "        ]\n" +
                "      }\n" +
                "    ],\n" +
                "    \"classes\": [\n" +
                "      {\n" +
                "        \"classVolume\": 30,\n" +
                "        \"classNumber\": 1,\n" +
                "        \"classType\": \"theory\"\n" +
                "      },\n" +
                "      {\n" +
                "        \"classVolume\": 30,\n" +
                "        \"classNumber\": 2,\n" +
                "        \"classType\": \"theory\"\n" +
                "      }\n" +
                "    ],\n" +
                "    \"educationGroupName\": \"Computer\",\n" +
                "    \"courseGroups\": [\n" +
                "      {\n" +
                "        \"courseYear\": 95,\n" +
                "        \"coach\": \"zahmatkesh\",\n" +
                "        \"presentedCourses\": [\n" +
                "          {\n" +
                "            \"id\": 1000,\n" +
                "            \"recurrences\": 1,\n" +
                "            \"teacher\": \"zahmatkesh\",\n" +
                "            \"courseName\": \"math\",\n" +
                "            \"units\": 3,\n" +
                "            \"courseType\": \"theory\",\n" +
                "            \"groupYear\": [\n" +
                "              95\n" +
                "            ]\n" +
                "          },\n" +
                "          {\n" +
                "            \"id\": 1001,\n" +
                "            \"recurrences\": 1,\n" +
                "            \"teacher\": \"farughi\",\n" +
                "            \"courseName\": \"physics\",\n" +
                "            \"units\": 3,\n" +
                "            \"courseType\": \"theory\",\n" +
                "            \"groupYear\": [\n" +
                "              95,\n" +
                "              96\n" +
                "            ]\n" +
                "          },\n" +
                "          {\n" +
                "            \"id\": 1001,\n" +
                "            \"recurrences\": 1,\n" +
                "            \"teacher\": \"farughi\",\n" +
                "            \"courseName\": \"physics 2\",\n" +
                "            \"units\": 3,\n" +
                "            \"courseType\": \"theory\",\n" +
                "            \"groupYear\": [\n" +
                "              95,\n" +
                "              96\n" +
                "            ]\n" +
                "          }\n" +
                "        ]\n" +
                "      },\n" +
                "      {\n" +
                "        \"courseYear\": 96,\n" +
                "        \"coach\": \"zahmatkesh\",\n" +
                "        \"presentedCourses\": [\n" +
                "          {\n" +
                "            \"id\": 1002,\n" +
                "            \"recurrences\": 1,\n" +
                "            \"teacher\": \"zahmatkesh\",\n" +
                "            \"courseName\": \"math2\",\n" +
                "            \"units\": 3,\n" +
                "            \"courseType\": \"theory\",\n" +
                "            \"groupYear\": [\n" +
                "              96\n" +
                "            ]\n" +
                "          },\n" +
                "          {\n" +
                "            \"id\": 1003,\n" +
                "            \"recurrences\": 1,\n" +
                "            \"teacher\": \"zahmatkesh\",\n" +
                "            \"courseName\": \"math3\",\n" +
                "            \"units\": 3,\n" +
                "            \"courseType\": \"theory\",\n" +
                "            \"groupYear\": [\n" +
                "              96\n" +
                "            ]\n" +
                "          },\n" +
                "          {\n" +
                "            \"id\": 1004,\n" +
                "            \"recurrences\": 1,\n" +
                "            \"teacher\": \"zahmatkesh\",\n" +
                "            \"courseName\": \"physics2\",\n" +
                "            \"units\": 3,\n" +
                "            \"courseType\": \"theory\",\n" +
                "            \"groupYear\": [\n" +
                "              96\n" +
                "            ]\n" +
                "          },\n" +
                "          {\n" +
                "            \"id\": 1005,\n" +
                "            \"recurrences\": 1,\n" +
                "            \"teacher\": \"farughi\",\n" +
                "            \"courseName\": \"physics3\",\n" +
                "            \"units\": 3,\n" +
                "            \"courseType\": \"theory\",\n" +
                "            \"groupYear\": [\n" +
                "              96\n" +
                "            ]\n" +
                "          }\n" +
                "        ]\n" +
                "      }\n" +
                "    ],\n" +
                "    \"entriesYears\": [\n" +
                "      95,\n" +
                "      96\n" +
                "    ]\n" +
                "  }\n" +
                "}"
        val all = generateSequence(operatingDates.start.atTime(10, 0)) { dt ->
            dt.plusMinutes(15).takeIf { it.plusMinutes(15) <= operatingDates.endInclusive.atTime(12, 0) }
        }.map { it..it.plusMinutes(15) }.toMutableList()

        println(all.toString())


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
