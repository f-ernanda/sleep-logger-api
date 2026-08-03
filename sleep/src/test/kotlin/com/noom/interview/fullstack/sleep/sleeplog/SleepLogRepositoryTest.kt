package com.noom.interview.fullstack.sleep.sleeplog

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.time.LocalDate
import java.time.ZoneOffset

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class SleepLogRepositoryTest {

    @Autowired
    private lateinit var repository: SleepLogRepository

    @Autowired
    private lateinit var jdbcTemplate: NamedParameterJdbcTemplate

    @BeforeEach
    fun cleanDatabase() {
        jdbcTemplate.jdbcTemplate.execute("TRUNCATE TABLE sleep_log RESTART IDENTITY")
    }

    @Test
    fun `create persists a sleep log and returns the generated id and createdAt`() {
        val toCreate = newSleepLog(userId = 1, logDate = LocalDate.of(2026, 7, 28), feeling = Feeling.GOOD)

        val created = repository.create(toCreate)

        assertThat(created.id).isPositive
        assertThat(created.createdAt).isNotNull
        assertThat(created.userId).isEqualTo(1L)
        assertThat(created.logDate).isEqualTo(LocalDate.of(2026, 7, 28))
        assertThat(created.feeling).isEqualTo(Feeling.GOOD)
        // Round-tripping through TIMESTAMPTZ is the trickiest part of this method — assert it
        // explicitly rather than trusting Timestamp.from()/toInstant() silently.
        assertThat(created.timeInBedStart).isEqualTo(toCreate.timeInBedStart)
        assertThat(created.timeInBedEnd).isEqualTo(toCreate.timeInBedEnd)
    }

    @Test
    fun `findLatestByUserId returns the most recent log for that user only`() {
        // Inserted in reverse logDate order on purpose: this log has the later logDate but the
        // lower id, so the test only passes if ordering is truly driven by logDate, not id.
        val expected = repository.create(newSleepLog(userId = 1, logDate = LocalDate.of(2026, 7, 28)))
        repository.create(newSleepLog(userId = 1, logDate = LocalDate.of(2026, 7, 20)))
        repository.create(newSleepLog(userId = 2, logDate = LocalDate.of(2026, 7, 29)))

        val latest = repository.findLatestByUserId(1)

        assertThat(latest?.id).isEqualTo(expected.id)
    }

    @Test
    fun `findLatestByUserId returns null when the user has no logs`() {
        assertThat(repository.findLatestByUserId(999)).isNull()
    }

    @Test
    fun `findByUserIdBetween returns only logs for that user within the date range, inclusive of both boundaries`() {
        val from = LocalDate.of(2026, 6, 28)
        val to = LocalDate.of(2026, 7, 28)

        val onStartBoundary = repository.create(newSleepLog(userId = 1, logDate = from))
        val onEndBoundary = repository.create(newSleepLog(userId = 1, logDate = to))
        val inRange = repository.create(newSleepLog(userId = 1, logDate = LocalDate.of(2026, 7, 15)))
        repository.create(newSleepLog(userId = 1, logDate = from.minusDays(1))) // just before the range
        repository.create(newSleepLog(userId = 1, logDate = to.plusDays(1))) // just after the range
        repository.create(newSleepLog(userId = 2, logDate = LocalDate.of(2026, 7, 15))) // other user

        val results = repository.findByUserIdBetween(1, from, to)

        assertThat(results).extracting("id")
            .containsExactlyInAnyOrder(onStartBoundary.id, onEndBoundary.id, inRange.id)
    }

    private fun newSleepLog(userId: Long, logDate: LocalDate, feeling: Feeling = Feeling.OK) = NewSleepLog(
        userId = userId,
        logDate = logDate,
        timeInBedStart = logDate.minusDays(1).atTime(23, 0).toInstant(ZoneOffset.UTC),
        timeInBedEnd = logDate.atTime(7, 0).toInstant(ZoneOffset.UTC),
        feeling = feeling
    )

    companion object {
        @Container
        @JvmStatic
        val postgres: PostgreSQLContainer<*> = PostgreSQLContainer<Nothing>("postgres:13-alpine")

        @DynamicPropertySource
        @JvmStatic
        fun postgresProperties(registry: DynamicPropertyRegistry) {
            registry.add("spring.datasource.url") { postgres.jdbcUrl }
            registry.add("spring.datasource.username") { postgres.username }
            registry.add("spring.datasource.password") { postgres.password }
        }
    }
}
