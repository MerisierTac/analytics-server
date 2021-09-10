package fr.gouv.tac.analytics.controller

import fr.gouv.tac.analytics.api.model.ErrorResponse
import fr.gouv.tac.analytics.api.model.ErrorResponseErrors
import org.slf4j.LoggerFactory
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.http.ResponseEntity
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
        val message = "Request body contains invalid attributes"
        val fieldErrors =
            ex.fieldErrors.map { ErrorResponseErrors(it.field, it.code, it.defaultMessage) }
        val globalErrors =
            ex.globalErrors.map { ErrorResponseErrors("", it.code, it.defaultMessage) }
        val errorResponseBody = ErrorResponse(
            status = BAD_REQUEST.value(),
            error = BAD_REQUEST.reasonPhrase,
            message = message,
            timestamp = now(),
            path = servletRequest.requestURI,
            errors = fieldErrors + globalErrors
        )
        log.warn("$message $fieldErrors $globalErrors")
        return ResponseEntity(errorResponseBody, BAD_REQUEST)
    }

    @ExceptionHandler
    fun handle(
        ex: ConstraintViolationException,
        request: HttpServletRequest
    ): ResponseEntity<ErrorResponse> {

        val message = "Request body contains invalid attributes"
        val errors = ex.constraintViolations.map {
            ErrorResponseErrors(
                field = it.propertyPath.toString(),
                code = it.constraintDescriptor.annotation.annotationClass.simpleName,
                message = it.message
            )
        }

        val errorResponseBody = ErrorResponse(
            status = BAD_REQUEST.value(),
            error = BAD_REQUEST.reasonPhrase,
            message = message,
            timestamp = now(),
            path = request.requestURI,
            errors = errors
        )
        log.warn("$message $errors")
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

        if (status.is5xxServerError) {
            log.error(ex.message)
        } else {
            log.warn(ex.message)
        }

        return ResponseEntity(errorResponseBody, status)
    }
}
