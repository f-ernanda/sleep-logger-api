package com.noom.interview.fullstack.sleep.sleeplog

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime

class SleepAverageCalculatorTest {

    private val rangeStart = LocalDate.of(2026, 6, 28)
    private val rangeEnd = LocalDate.of(2026, 7, 28)

    @Test
    fun `returns zeroed averages and no bed or wake time when there are no logs`() {
        val averages = SleepAverageCalculator.calculate(emptyList(), rangeStart, rangeEnd)

        assertThat(averages.averageTotalTimeInBedMinutes).isEqualTo(0.0)
        assertThat(averages.averageBedTime).isNull()
        assertThat(averages.averageWakeTime).isNull()
        assertThat(averages.feelingFrequency).containsEntry(Feeling.BAD, 0)
            .containsEntry(Feeling.OK, 0)
            .containsEntry(Feeling.GOOD, 0)
    }

    @Test
    fun `averages total time in bed across logs`() {
        val logs = listOf(
            sleepLog(start = "2026-07-27T23:00:00Z", end = "2026-07-28T07:00:00Z"), // 480 min
            sleepLog(start = "2026-07-26T23:00:00Z", end = "2026-07-27T06:00:00Z")  // 420 min
        )

        val averages = SleepAverageCalculator.calculate(logs, rangeStart, rangeEnd)

        assertThat(averages.averageTotalTimeInBedMinutes).isEqualTo(450.0)
    }

    @Test
    fun `averages bed and wake times across midnight without skewing toward noon`() {
        val logs = listOf(
            sleepLog(start = "2026-07-27T23:00:00Z", end = "2026-07-28T07:00:00Z"),
            sleepLog(start = "2026-07-26T01:00:00Z", end = "2026-07-26T09:00:00Z")
        )

        val averages = SleepAverageCalculator.calculate(logs, rangeStart, rangeEnd)

        // Naively averaging minutes-since-midnight for 23:00 and 01:00 would wrongly give
        // noon; the correct circular average is midnight.
        assertThat(averages.averageBedTime).isEqualTo(LocalTime.of(0, 0))
        assertThat(averages.averageWakeTime).isEqualTo(LocalTime.of(8, 0))
    }

    @Test
    fun `counts feeling frequency including feelings that never occurred`() {
        val logs = listOf(
            sleepLog(start = "2026-07-27T23:00:00Z", end = "2026-07-28T07:00:00Z", feeling = Feeling.GOOD),
            sleepLog(start = "2026-07-26T23:00:00Z", end = "2026-07-27T07:00:00Z", feeling = Feeling.GOOD),
            sleepLog(start = "2026-07-25T23:00:00Z", end = "2026-07-26T07:00:00Z", feeling = Feeling.BAD)
        )

        val averages = SleepAverageCalculator.calculate(logs, rangeStart, rangeEnd)

        assertThat(averages.feelingFrequency).containsEntry(Feeling.GOOD, 2)
            .containsEntry(Feeling.BAD, 1)
            .containsEntry(Feeling.OK, 0)
    }

    private fun sleepLog(start: String, end: String, feeling: Feeling = Feeling.OK) = SleepLog(
        id = 1,
        userId = 1,
        logDate = LocalDate.of(2026, 7, 28),
        timeInBedStart = Instant.parse(start),
        timeInBedEnd = Instant.parse(end),
        feeling = feeling,
        createdAt = Instant.parse(end)
    )
}
