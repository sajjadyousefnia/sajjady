package calculations

import com.example.breaks
import com.example.operatingDates
import com.example.operatingDay
import java.time.LocalDateTime

data class Block(val range: ClosedRange<LocalDateTime>) {

    val timeRange = range.start.toLocalTime()..range.endInclusive.toLocalTime()

    /** indicates if this block is zeroed due to operating day/break constraints */
    val withinOperatingDay
        get() = breaks.all { timeRange.start !in it } &&
                timeRange.start in operatingDay &&
                timeRange.endInclusive in operatingDay

    /* All operating blocks for the entire week, broken up in 15 minute increments */
    var all =
        generateSequence(operatingDates.start.atStartOfDay()) { dt ->
            dt.plusMinutes(120).takeIf { it.plusMinutes(120) <= operatingDates.endInclusive.atTime(23, 59) }
        }.map { Block(it..it.plusMinutes(120)) }
            .toList()


    /* only returns blocks within the operating times */
    val allInOperatingDay by lazy {
        all.filter { it.withinOperatingDay }

    }
}
