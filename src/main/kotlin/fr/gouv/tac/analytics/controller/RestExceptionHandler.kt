package fr.gouv.tac.analytics.controller

import com.fasterxml.jackson.databind.exc.MismatchedInputException
import fr.gouv.tac.analytics.api.model.ErrorResponse
import fr.gouv.tac.analytics.api.model.ErrorResponseErrors
import org.slf4j.LoggerFactory
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.stereotype.Controller
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.context.request.WebRequest
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler
import java.time.OffsetDateTime.now
import javax.servlet.http.HttpServletRequest
import javax.validation.ConstraintViolationException

@RestControllerAdvice(annotations = [Controller::class])
class RestExceptionHandler(private val servletRequest: HttpServletRequest) : ResponseEntityExceptionHandler() {

    private val log = LoggerFactory.getLogger(RestExceptionHandler::class.java)

    override fun handleMethodArgumentNotValid(
        ex: MethodArgumentNotValidException,
        headers: HttpHeaders,
        status: HttpStatus,
        request: WebRequest
    ): ResponseEntity<Any> {
        val fieldErrors =
            ex.fieldErrors.map { ErrorResponseErrors(it.field, it.code, it.defaultMessage) }
        val globalErrors =
            ex.globalErrors.map { ErrorResponseErrors("", it.code, it.defaultMessage) }
        return badRequestAndLogErrors(fieldErrors + globalErrors)
    }

    @ExceptionHandler
    fun handle(ex: ConstraintViolationException): ResponseEntity<Any> {
        val errors = ex.constraintViolations.map {
            ErrorResponseErrors(
                field = it.propertyPath.toString(),
                code = it.constraintDescriptor.annotation.annotationClass.simpleName,
                message = it.message
            )
        }
        return badRequestAndLogErrors(errors)
    }

    override fun handleHttpMessageNotReadable(
        ex: HttpMessageNotReadableException,
        headers: HttpHeaders,
        status: HttpStatus,
        request: WebRequest
    ): ResponseEntity<Any> {
        val cause = ex.cause
        val error = if (cause is MismatchedInputException) {
            ErrorResponseErrors(
                field = cause.path
                    .map { if (it.fieldName != null) ".${it.fieldName}" else "[${it.index}]" }
                    .joinToString("")
                    .removePrefix("."),
                code = "HttpMessageNotReadable",
                message = ex.rootCause!!.message
            )
        } else {
            ErrorResponseErrors(
                field = "",
                code = ex::class.simpleName,
                message = ex.message
            )
        }
        return badRequestAndLogErrors(listOf(error))
    }

    private fun badRequestAndLogErrors(errors: List<ErrorResponseErrors>): ResponseEntity<Any> {
        val errorResponseBody = ErrorResponse(
            status = BAD_REQUEST.value(),
            error = BAD_REQUEST.reasonPhrase,
            message = "Request body contains invalid attributes",
            timestamp = now(),
            path = servletRequest.requestURI,
            errors = errors
        )
        errorResponseBody.errors?.forEach {
            log.info("Validation error on {} {}: '{}' {}", servletRequest.method, servletRequest.requestURI, it.field, it.message)
        }
        return ResponseEntity(errorResponseBody, BAD_REQUEST)
    }

    override fun handleExceptionInternal(
        ex: Exception,
        body: Any?,
        headers: HttpHeaders,
        status: HttpStatus,
        request: WebRequest
    ): ResponseEntity<Any> {
        super.handleExceptionInternal(ex, body, headers, status, request)
        val errorResponseBody = ErrorResponse(
            status = status.value(),
            error = status.reasonPhrase,
            message = ex.message ?: "Internal error",
            timestamp = now(),
            path = servletRequest.requestURI
        )

        if (status.is4xxClientError) {
            log.info("Client side error on {} {}: {}", servletRequest.method, servletRequest.requestURI, ex.message)
        } else {
            log.error("An unexpected error occured", ex)
        }

        return ResponseEntity(errorResponseBody, status)
    }
}
