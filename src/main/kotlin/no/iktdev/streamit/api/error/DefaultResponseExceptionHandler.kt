package no.iktdev.streamit.api.error

import no.iktdev.streamit.api.classes.Response
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.context.request.WebRequest
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler
import java.lang.Exception

@ControllerAdvice
@RestController
class DefaultResponseExceptionHandler: ResponseEntityExceptionHandler() {

    @ExceptionHandler
    final fun handleGeneralException(exception: Exception, request: WebRequest): ResponseEntity<Response>
    {
        val response = Response(false, exception.message.toString())
        return ResponseEntity(response, HttpStatus.NOT_ACCEPTABLE)
    }
}