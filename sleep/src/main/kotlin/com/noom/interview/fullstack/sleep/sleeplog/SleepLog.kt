package com.noom.interview.fullstack.sleep.sleeplog

import java.time.Duration
import java.time.Instant
import java.time.LocalDate

enum class Feeling { BAD, OK, GOOD }

data class SleepLog(
    val id: Long,
    val userId: Long,
    val logDate: LocalDate,
    val timeInBedStart: Instant,
    val timeInBedEnd: Instant,
    val feeling: Feeling,
    val createdAt: Instant
) {
    val totalTimeInBedMinutes: Long
        get() = Duration.between(timeInBedStart, timeInBedEnd).toMinutes()
}

data class NewSleepLog(
    val userId: Long,
    val logDate: LocalDate,
    val timeInBedStart: Instant,
    val timeInBedEnd: Instant,
    val feeling: Feeling
)
