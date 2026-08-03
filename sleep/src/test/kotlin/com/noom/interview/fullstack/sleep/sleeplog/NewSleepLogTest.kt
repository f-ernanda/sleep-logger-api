package com.noom.interview.fullstack.sleep.sleeplog

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatIllegalArgumentException
import org.junit.jupiter.api.Test
import java.time.Instant
import java.time.LocalDate

class NewSleepLogTest {

    @Test
    fun `accepts an interval where end is after start`() {
        val newSleepLog = NewSleepLog(
            userId = 1,
            logDate = LocalDate.of(2026, 7, 28),
            timeInBedStart = Instant.parse("2026-07-27T23:15:00Z"),
            timeInBedEnd = Instant.parse("2026-07-28T07:00:00Z"),
            feeling = Feeling.GOOD
        )

        assertThat(newSleepLog.timeInBedEnd).isAfter(newSleepLog.timeInBedStart)
    }

    @Test
    fun `rejects an interval where end equals start`() {
        val instant = Instant.parse("2026-07-28T07:00:00Z")

        assertThatIllegalArgumentException().isThrownBy {
            NewSleepLog(
                userId = 1,
                logDate = LocalDate.of(2026, 7, 28),
                timeInBedStart = instant,
                timeInBedEnd = instant,
                feeling = Feeling.GOOD
            )
        }
    }

    @Test
    fun `rejects an interval where end is before start`() {
        assertThatIllegalArgumentException().isThrownBy {
            NewSleepLog(
                userId = 1,
                logDate = LocalDate.of(2026, 7, 28),
                timeInBedStart = Instant.parse("2026-07-28T07:00:00Z"),
                timeInBedEnd = Instant.parse("2026-07-27T23:15:00Z"),
                feeling = Feeling.GOOD
            )
        }
    }
}
