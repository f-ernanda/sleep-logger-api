package com.noom.interview.fullstack.sleep.error

import com.noom.interview.fullstack.sleep.sleeplog.SleepLogNotFoundException
import org.springframework.http.HttpStatus
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.HttpRequestMethodNotSupportedException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException
import org.springframework.web.servlet.NoHandlerFoundException

@RestControllerAdvice
class RestExceptionHandler {

    @ExceptionHandler(SleepLogNotFoundException::class, NoHandlerFoundException::class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    fun handleNotFound(ex: Exception): ApiError = ApiError(ex.message ?: "Not found")

    @ExceptionHandler(HttpMessageNotReadableException::class, MethodArgumentTypeMismatchException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleBadRequest(ex: Exception): ApiError = ApiError("Malformed request")

    @ExceptionHandler(IllegalArgumentException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleInvalidArgument(ex: IllegalArgumentException): ApiError = ApiError(ex.message ?: "Invalid request")

    @ExceptionHandler(HttpRequestMethodNotSupportedException::class)
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    fun handleMethodNotAllowed(ex: HttpRequestMethodNotSupportedException): ApiError = ApiError(
        ex.message ?: "Method not allowed"
    )

    @ExceptionHandler(Exception::class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    fun handleUnexpected(ex: Exception): ApiError = ApiError("Unexpected error")
}
