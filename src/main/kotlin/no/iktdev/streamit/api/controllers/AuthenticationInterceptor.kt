package no.iktdev.streamit.api.controllers

import no.iktdev.streamit.api.controllers.annotations.Authentication
import no.iktdev.streamit.api.controllers.annotations.AuthenticationModes
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Component
import org.springframework.web.method.HandlerMethod
import org.springframework.web.servlet.HandlerInterceptor
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse


@Component
class AuthenticationInterceptor: HandlerInterceptor, Authy() {
    override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {
        val validation = (handler as HandlerMethod).method.getAnnotation(Authentication::class.java)
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