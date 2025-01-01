package no.iktdev.streamit.api.controllers

import no.iktdev.streamit.api.controllers.annotations.Authentication
import no.iktdev.streamit.api.controllers.annotations.AuthenticationModes
import no.iktdev.streamit.api.getRequestersIp
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Component
import org.springframework.web.method.HandlerMethod
import org.springframework.web.servlet.HandlerInterceptor
import org.springframework.web.servlet.resource.ResourceHttpRequestHandler
import java.util.stream.Collectors
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse


@Component
class AuthenticationInterceptor: HandlerInterceptor, Authy() {
    override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {
        val validation = try {
            if (handler is HandlerMethod)
                (handler as HandlerMethod).method.getAnnotation(Authentication::class.java)
            else null
        } catch (e: Exception) {
            e.printStackTrace()
            val url = request.requestURL.toString()
            val queryParams = request.queryString
            val body = request.reader.lines().collect(Collectors.joining(System.lineSeparator()))
            log.error { "Error report:\n\tSource:${request.getRequestersIp()}\n\tUrl:$url\n\tQuery params:$queryParams\n\tBody:$body" }
            null
        }
        return if (validation == null) {
            super.preHandle(request, response, handler)
        } else {
            val value = request.getHeader("Authorization")
            val valid = when(validation.mode) {
                AuthenticationModes.SOFT -> {
                    val isValid = isValid(value)
                    if (!isValid)
                        response.status = HttpStatus.BAD_REQUEST.value()
                    isValid
                }
                AuthenticationModes.STRICT -> {
                    val isValid = isValid(value)
                    if (!isValid)
                        response.status = HttpStatus.BAD_REQUEST.value()
                    // TODO: Stricter lookup + check user is correct and present
                    isValid
                }
                AuthenticationModes.NONE -> {
                    log.warn { "Allowing request through ${this::class.java.simpleName} due to authentication mode set to None" }
                    return true
                }
                else -> {
                    response.status = HttpStatus.SERVICE_UNAVAILABLE.value()
                    false
                }
            }
            if (valid) LoggerFactory.getLogger(javaClass.simpleName).info("Auth interceptor accepted") else LoggerFactory.getLogger(javaClass.simpleName).error("Auth interceptor rejected!")
            valid
        }
    }
}