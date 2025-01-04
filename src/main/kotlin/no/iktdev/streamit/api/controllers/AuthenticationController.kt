package no.iktdev.streamit.api.controllers

import com.google.api.Http
import com.google.gson.Gson
import no.iktdev.streamit.api.classes.Jwt
import no.iktdev.streamit.api.classes.User
import no.iktdev.streamit.api.classes.remote.*
import no.iktdev.streamit.api.controllers.annotations.Authentication
import no.iktdev.streamit.api.controllers.annotations.AuthenticationModes
import no.iktdev.streamit.api.database.queries.QUser
import no.iktdev.streamit.api.database.timestampToLocalDateTime
import no.iktdev.streamit.api.database.toEpochSeconds
import no.iktdev.streamit.library.db.executeWithResult
import no.iktdev.streamit.library.db.executeWithStatus
import no.iktdev.streamit.library.db.tables.AuthMethod
import no.iktdev.streamit.library.db.tables.delegatedAuthenticationTable
import no.iktdev.streamit.library.db.tables.registeredDevices
import no.iktdev.streamit.library.db.withTransaction
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.LocalDateTime
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

open class AuthenticationController: Authy() {

    open fun createJWT(@RequestBody user: User): Jwt {
        return createJwt(user)
    }


    open fun validateToken(request: HttpServletRequest? = null): ResponseEntity<Boolean?> {
        val token = request?.getHeader("Authorization") ?: return ResponseEntity.internalServerError().body(null)
        val isValid = super.isValid(token)
        return if (isValid) {
            ResponseEntity.status(202).body(isValid)
        } else {
            ResponseEntity.status(405).body(isValid)
        }
    }



    @RestController
    @RequestMapping(path = ["/open/auth"])
    class OpenAuthentication: AuthenticationController() {
        @PostMapping(value = ["/new"])
        override fun createJWT(@RequestBody user: User): Jwt {
            return super.createJWT(user)
        }

        @GetMapping(value = ["/validate"])
        override fun validateToken(request: HttpServletRequest?): ResponseEntity<Boolean?> {
            return super.validateToken(request)
        }

    }

    @RestController
    @RequestMapping(path = ["/secure/auth"])
    class RestrictedAuthentication: AuthenticationController() {

        @PostMapping(value = ["/new"])
        @Authentication(AuthenticationModes.STRICT)
        override fun createJWT(@RequestBody user: User): Jwt {
            return super.createJWT(user)
        }

        @GetMapping(value = ["/validate"])
        @Authentication(AuthenticationModes.STRICT)
        override fun validateToken(request: HttpServletRequest?): ResponseEntity<Boolean?> {
            return super.validateToken(request)
        }


        @Authentication(AuthenticationModes.STRICT)
        @PostMapping(value = ["/new/cast"])
        fun createCastJWT(
            @RequestHeader("Authorization") bearer: String, response: HttpServletResponse
        ): Jwt? {
            val result = decode(bearer)
            if (result == null) {
                response.sendError(HttpStatus.BAD_REQUEST.value(), "Unable to decode JWT")
                return null
            }
            val payloadUser: User? = getUser(result)
            if (payloadUser == null) {
                response.sendError(HttpStatus.BAD_REQUEST.value(), "Unable to find user in JWT")
                return null
            }

            val user = QUser().selectWidth(payloadUser.guid)
            if (user == null) {
                response.sendError(HttpStatus.BAD_REQUEST.value(), "Unable to find user found in JWT in system")
                return null
            }

            return createJwt(user, "3h")
        }
    }
}