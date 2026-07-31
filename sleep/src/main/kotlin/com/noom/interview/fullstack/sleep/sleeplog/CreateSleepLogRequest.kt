package com.noom.interview.fullstack.sleep.sleeplog

import java.time.Instant
import java.time.LocalDate

data class CreateSleepLogRequest(
    val logDate: LocalDate,
    val timeInBedStart: Instant,
    val timeInBedEnd: Instant,
    val feeling: Feeling
) {
    fun toNewSleepLog(userId: Long) = NewSleepLog(
        userId = userId,
        logDate = logDate,
        timeInBedStart = timeInBedStart,
        timeInBedEnd = timeInBedEnd,
        feeling = feeling
    )
}
