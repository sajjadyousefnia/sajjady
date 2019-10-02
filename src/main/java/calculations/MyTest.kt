package calculations

object MyTest {
    @JvmStatic
    fun main(args: Array<String>) {
        val x =
            arrayListOf/*<MyData>*/(
                1, 1, 2, 2, 3, 3, 4, 4
                /* MyData("reza", "2015", "asasas"),
                 MyData("hasan", "2006", "sasas"),
                 MyData("taghi", "2019", "asasa"),
                 MyData("zeynab", "1993", "asassssa"),
                 MyData("sina", "1950", "asasasaddds"),
                 MyData("taghi", "2019", "asdsdsdsasasadddsassas")
                 , MyData("reza", "2015", "asdsdsdsasasadddsassas")*/
            )
        //  println(x.map { it.name to it.year }.distinct())
        x.remove(x.first { it == 3 })
        println(x)
        // println((x.map { it.name }).zip(x.map { it.year }))
    }
}

data class MyData(val name: String, val year: String, val etc: String)
