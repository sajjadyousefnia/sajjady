package com.example


import java.time.LocalDateTime


/** A discrete, 15-minute chunk of time a class can be scheduled on */
data class Block(val range: ClosedRange<LocalDateTime>) {

    val timeRange = range.start.toLocalTime()..range.endInclusive.toLocalTime()

    /** indicates if this block is zeroed due to operating day/break constraints */
    val withinOperatingDay
        get() = breaks.all { timeRange.start !in it } &&
                timeRange.start in operatingDay &&
                timeRange.endInclusive in operatingDay

    val affectingSlots by lazy {
        ScheduledClass.all!!.asSequence()
            .flatMap {
                it.affectingSlotsFor(this).asSequence()
            }.toSet()
    }

    companion object {
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
}


data class ScheduledClass(
    val id: Int,
    var teacher: String = "",
    var groupYear: Int = 0,
    var classRoom: Int = 0,
    val name: String,
    val hoursLength: Double,
    val recurrences: Int,
    val recurrenceGapDays: Int = 2
) {

    /** the # of slots between each recurrence */
    val gap = recurrenceGapDays * 12/*24 * 4*/

    /** the # of slots needed for a given occurrence */
    val slotsNeededPerSession = /*(hoursLength * 4).toInt()*/1

    /** yields slots for this given scheduled class */
    val slots by lazy {
        Slot.all.asSequence().filter { it.scheduledClass == this }.toList()
    }

    /** yields slot groups for this scheduled class */
    val recurrenceSlots by lazy {
        slots.affectedWindows(
            slotsNeeded = slotsNeededPerSession,
            gap = gap,
            recurrences = recurrences,
            mode = RecurrenceMode.FULL_ONLY
        ).toList()
    }

    /** yields slots that affect the given block for this scheduled class */
    fun affectingSlotsFor(block: Block) = recurrenceSlots.asSequence()
        .filter { blk -> blk.flatMap { it }.any { it.block == block } }
        .map { it.first().first() }

    /** These slots should be fixed to zero **/
    val slotsFixedToZero by lazy {
        // broken recurrences
        slots.affectedWindows(
            slotsNeeded = slotsNeededPerSession,
            gap = gap,
            recurrences = recurrences,
            mode = RecurrenceMode.PARTIAL_ONLY
        ).flatMap { it.asSequence() }
            .flatMap { it.asSequence() }
            // operating day breaks
            // affected slots that cross into non-operating day
            .plus(
                recurrenceSlots.asSequence()
                    .flatMap { it.asSequence() }
                    .filter { slot -> slot.any { !it.block.withinOperatingDay } }
                    .map { it.first() }
            )
            .distinct()
            .onEach {
                it.selected = 0
            }
            .toList()
    }

    /** translates and returns the optimized start time of the class */
    val start get() = slots.asSequence().filter { it.selected == 1 }.map { it.block.range.start }.min()!!

    /** translates and returns the optimized end time of the class */
    val end get() = start.plusMinutes((hoursLength * 60.0).toLong())

    /** returns the DayOfWeeks where recurrences take place */
    val daysOfWeek get() = (0..(recurrences - 1)).asSequence().map { start.dayOfWeek.plus(it.toLong() * recurrenceGapDays) }.sorted()

    companion object {
        val all = scheduledClasses
    }
}


data class Slot(val block: Block, val scheduledClass: ScheduledClass) {

    var selected: Int? = null

    companion object {
        var all =
            Block.all.asSequence().flatMap { b ->
                ScheduledClass.all.asSequence().map { Slot(b, it) }
            }.toList()

    }
}

enum class RecurrenceMode { PARTIAL_ONLY, FULL_ONLY, ALL }

fun <T> List<T>.affectedWindows(
    slotsNeeded: Int,
    gap: Int,
    recurrences: Int,
    mode: RecurrenceMode = RecurrenceMode.FULL_ONLY
) =
    (0..size).asSequence().map { i ->
        (1..recurrences).asSequence().map { (it - 1) * gap }
            .filter { it + i < size }
            .map { r ->
                subList(i + r, (i + r + slotsNeeded).let { if (it > size) size else it })
            }
            .toList()
    }.filter {
        when (mode) {
            RecurrenceMode.ALL -> true
            RecurrenceMode.FULL_ONLY -> it.size == recurrences && it.all { it.size == slotsNeeded }
            RecurrenceMode.PARTIAL_ONLY -> it.size < recurrences || it.any { it.size < slotsNeeded }
        }
    }


fun main(args: Array<String>) {
    (1..20).toList()
        .affectedWindows(
            slotsNeeded = 4,
            gap = 6,
            recurrences = 3,
            mode = RecurrenceMode.PARTIAL_ONLY
        )
        .forEach { println(it) }
}