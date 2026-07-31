package com.noom.interview.fullstack.sleep.sleeplog

class SleepLogNotFoundException(userId: Long) : RuntimeException("No sleep log found for user $userId")
