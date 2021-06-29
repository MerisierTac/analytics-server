package fr.gouv.tac.analytics.controller

import fr.gouv.tac.analytics.api.model.ErrorResponse
import fr.gouv.tac.analytics.api.model.ErrorResponseErrors
import lombok.RequiredArgsConstructor
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.FieldError
import org.springframework.validation.ObjectError
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.context.request.WebRequest
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler
import java.time.OffsetDateTime
import java.util.stream.Collectors
import java.util.stream.Stream
import javax.servlet.http.HttpServletRequest
import javax.validation.ConstraintViolation
import javax.validation.ConstraintViolationException

@RestControllerAdvice
@RequiredArgsConstructor
class RestExceptionHandler : ResponseEntityExceptionHandler() {
    private val servletRequest: HttpServletRequest? = null
    override fun handleMethodArgumentNotValid(
        ex: MethodArgumentNotValidException,
        headers: HttpHeaders, status: HttpStatus, request: WebRequest
    ): ResponseEntity<Any> {
        val fieldErrors = ex.fieldErrors.stream()
            .map { err: FieldError -> ErrorResponseErrors(err.field, err.code, err.defaultMessage) }
        val globalErrors = ex.globalErrors.stream()
            .map { err: ObjectError -> ErrorResponseErrors("", err.code, err.defaultMessage) }
        val errorResponseBody = ErrorResponse(
            status = HttpStatus.BAD_REQUEST.value(),
            error = HttpStatus.BAD_REQUEST.reasonPhrase,
            message = "Request body contains invalid attributes",
            timestamp = OffsetDateTime.now(),
            path = servletRequest!!.requestURI,
            errors = Stream.concat(fieldErrors, globalErrors).collect(Collectors.toList())
        )
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST.value())
            .body(errorResponseBody)
    }

    @ExceptionHandler
    fun handle(
        ex: ConstraintViolationException,
        request: HttpServletRequest
    ): ResponseEntity<ErrorResponse> {

        val errors = ex.constraintViolations.stream()
            .map { err: ConstraintViolation<*> ->
                ErrorResponseErrors(
                    field = err.propertyPath.toString(),
                    code = err.constraintDescriptor.annotation.annotationClass.simpleName,
                    message = err.message
                )
            }
            .collect(Collectors.toList())

        val errorResponseBody = ErrorResponse(
            status = HttpStatus.BAD_REQUEST.value(),
            error = HttpStatus.BAD_REQUEST.reasonPhrase,
            message = "Request body contains invalid attributes",
            timestamp = OffsetDateTime.now(),
            path = request.requestURI,
            errors = errors
        )
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST.value())
            .body(errorResponseBody)
    }

    override fun handleExceptionInternal(
        ex: Exception, body: Any?, headers: HttpHeaders,
        status: HttpStatus, request: WebRequest
    ): ResponseEntity<Any> {
        super.handleExceptionInternal(ex, body, headers, status, request)
        val errorResponseBody = ErrorResponse(
            status = status.value(),
            error = status.reasonPhrase,
            message = ex.message ?: "",
            timestamp = OffsetDateTime.now(),
            path = servletRequest!!.requestURI
        )
        return ResponseEntity(errorResponseBody, status)
    }
}