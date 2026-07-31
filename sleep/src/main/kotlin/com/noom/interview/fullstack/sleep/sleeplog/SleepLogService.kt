package com.noom.interview.fullstack.sleep.sleeplog

import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.ZoneOffset

@Service
class SleepLogService(private val repository: SleepLogRepository) {

    fun create(newSleepLog: NewSleepLog): SleepLog = repository.create(newSleepLog)

    fun getLatest(userId: Long): SleepLog? = repository.findLatestByUserId(userId)

    fun getAverages(userId: Long, today: LocalDate = LocalDate.now(ZoneOffset.UTC)): SleepAverages {
        val rangeStart = today.minusDays(30)
        val logs = repository.findByUserIdBetween(userId, rangeStart, today)
        return SleepAverageCalculator.calculate(logs, rangeStart, today)
    }
}
