package com.noom.interview.fullstack.sleep.sleeplog

import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneOffset

data class SleepAverages(
    val rangeStart: LocalDate,
    val rangeEnd: LocalDate,
    val averageTotalTimeInBedMinutes: Double,
    val averageBedTime: LocalTime?,
    val averageWakeTime: LocalTime?,
    val feelingFrequency: Map<Feeling, Int>
)

object SleepAverageCalculator {

    fun calculate(logs: List<SleepLog>, rangeStart: LocalDate, rangeEnd: LocalDate): SleepAverages {
        return SleepAverages(
            rangeStart = rangeStart,
            rangeEnd = rangeEnd,
            averageTotalTimeInBedMinutes = if (logs.isEmpty()) 0.0 else logs.map { it.totalTimeInBedMinutes }.average(),
            averageBedTime = averageTimeOfDay(logs.map { it.timeInBedStart.toUtcLocalTime() }),
            averageWakeTime = averageTimeOfDay(logs.map { it.timeInBedEnd.toUtcLocalTime() }),
            feelingFrequency = Feeling.values().associateWith { feeling -> logs.count { it.feeling == feeling } }
        )
    }

    private fun Instant.toUtcLocalTime(): LocalTime = atZone(ZoneOffset.UTC).toLocalTime()

    // Naively averaging minutes-since-midnight breaks around midnight (23:00 and 01:00 would
    // average to noon). Shifting the reference point to noon before averaging keeps typical
    // bed/wake times on a single continuous scale, then the shift is undone on the result.
    private fun averageTimeOfDay(times: List<LocalTime>): LocalTime? {
        if (times.isEmpty()) return null
        val shiftedMinutes = times.map { (it.toSecondOfDay() / 60.0 + 720) % 1440 }
        val averageShifted = shiftedMinutes.average()
        val averageMinutes = (averageShifted - 720 + 1440) % 1440
        return LocalTime.ofSecondOfDay((averageMinutes * 60).toLong())
    }
}
