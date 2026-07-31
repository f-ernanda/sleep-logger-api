package com.noom.interview.fullstack.sleep.sleeplog

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/users/{userId}/sleep-logs")
class SleepLogController(private val service: SleepLogService) {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(@PathVariable userId: Long, @RequestBody request: CreateSleepLogRequest): SleepLog =
        service.create(request.toNewSleepLog(userId))

    @GetMapping("/latest")
    fun getLatest(@PathVariable userId: Long): SleepLog =
        service.getLatest(userId) ?: throw SleepLogNotFoundException(userId)

    @GetMapping("/averages")
    fun getAverages(@PathVariable userId: Long): SleepAverages =
        service.getAverages(userId)
}
