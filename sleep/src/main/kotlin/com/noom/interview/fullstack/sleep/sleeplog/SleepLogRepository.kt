package com.noom.interview.fullstack.sleep.sleeplog

import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.jdbc.core.simple.SimpleJdbcInsert
import org.springframework.stereotype.Repository
import java.sql.ResultSet
import java.sql.Timestamp
import java.time.LocalDate
import javax.sql.DataSource

@Repository
class SleepLogRepository(
    private val jdbcTemplate: NamedParameterJdbcTemplate,
    dataSource: DataSource
) {
    private val insert = SimpleJdbcInsert(dataSource)
        .withTableName("sleep_log")
        .usingGeneratedKeyColumns("id", "created_at")

    fun create(newSleepLog: NewSleepLog): SleepLog {
        val params = mapOf(
            "user_id" to newSleepLog.userId,
            "log_date" to newSleepLog.logDate,
            "time_in_bed_start" to Timestamp.from(newSleepLog.timeInBedStart),
            "time_in_bed_end" to Timestamp.from(newSleepLog.timeInBedEnd),
            "feeling" to newSleepLog.feeling.name
        )
        val keys = insert.executeAndReturnKeyHolder(params).keys!!
        return SleepLog(
            id = (keys["id"] as Number).toLong(),
            userId = newSleepLog.userId,
            logDate = newSleepLog.logDate,
            timeInBedStart = newSleepLog.timeInBedStart,
            timeInBedEnd = newSleepLog.timeInBedEnd,
            feeling = newSleepLog.feeling,
            createdAt = (keys["created_at"] as Timestamp).toInstant()
        )
    }

    fun findLatestByUserId(userId: Long): SleepLog? {
        val sql = """
            SELECT id, user_id, log_date, time_in_bed_start, time_in_bed_end, feeling, created_at
            FROM sleep_log
            WHERE user_id = :userId
            ORDER BY log_date DESC, id DESC
            LIMIT 1
        """.trimIndent()
        return jdbcTemplate.query(sql, mapOf("userId" to userId), ::mapRow).firstOrNull()
    }

    fun findByUserIdBetween(userId: Long, from: LocalDate, to: LocalDate): List<SleepLog> {
        val sql = """
            SELECT id, user_id, log_date, time_in_bed_start, time_in_bed_end, feeling, created_at
            FROM sleep_log
            WHERE user_id = :userId AND log_date BETWEEN :from AND :to
            ORDER BY log_date DESC, id DESC
        """.trimIndent()
        val params = mapOf("userId" to userId, "from" to from, "to" to to)
        return jdbcTemplate.query(sql, params, ::mapRow)
    }

    private fun mapRow(rs: ResultSet, rowNum: Int): SleepLog = SleepLog(
        id = rs.getLong("id"),
        userId = rs.getLong("user_id"),
        logDate = rs.getDate("log_date").toLocalDate(),
        timeInBedStart = rs.getTimestamp("time_in_bed_start").toInstant(),
        timeInBedEnd = rs.getTimestamp("time_in_bed_end").toInstant(),
        feeling = Feeling.valueOf(rs.getString("feeling")),
        createdAt = rs.getTimestamp("created_at").toInstant()
    )
}
