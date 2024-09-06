package no.iktdev.streamit.api.controllers

import no.iktdev.streamit.api.classes.Jwt
import no.iktdev.streamit.api.classes.User
import no.iktdev.streamit.api.classes.remote.DelegatedEntryData
import no.iktdev.streamit.api.classes.remote.delegatedAuthenticationData
import no.iktdev.streamit.api.controllers.annotations.Authentication
import no.iktdev.streamit.api.controllers.annotations.AuthenticationModes
import no.iktdev.streamit.api.database.queries.QUser
import no.iktdev.streamit.api.database.timestampToLocalDateTime
import no.iktdev.streamit.api.database.toEpochSeconds
import no.iktdev.streamit.library.db.executeWithResult
import no.iktdev.streamit.library.db.executeWithStatus
import no.iktdev.streamit.library.db.tables.delegatedAuthenticationTable
import no.iktdev.streamit.library.db.tables.registeredDevices
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.LocalDateTime
import javax.servlet.http.HttpServletResponse

open class AuthenticationController: Authy() {

    open fun createJWT(@RequestBody user: User): Jwt {
        return createJwt(user)
    }

    /**
     * This function is for a remote device to start polling once it received the server configuration
     * It will pull on its own requester id as primary path, then pincode as secondary
     */
    open fun createDelegatedJwt(@PathVariable requesterId: String, @PathVariable pin: String): ResponseEntity<Jwt?> {
        val result = try {
            transaction {
                val record = delegatedAuthenticationTable.select {
                    (delegatedAuthenticationTable.pin eq pin) and
                            (delegatedAuthenticationTable.requesterId eq requesterId) and
                            (delegatedAuthenticationTable.consumed eq false)
                }.firstNotNullOfOrNull {
                    delegatedAuthenticationData(
                        pin = it[delegatedAuthenticationTable.pin],
                        requesterId = it[delegatedAuthenticationTable.requesterId],
                        created = it[delegatedAuthenticationTable.created],
                        expires = it[delegatedAuthenticationTable.expires]
                    )
                }
                if (record != null) {
                    // Consume
                    delegatedAuthenticationTable.update({
                        (delegatedAuthenticationTable.requesterId eq record.requesterId) and
                        (delegatedAuthenticationTable.pin eq record.pin)
                    }) {
                        it[consumed] = true
                    }
                }
                record
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
        if (result == null) {
            return ResponseEntity.notFound().build()
        }
        return if (result.expires > LocalDateTime.now()) {
            ResponseEntity.ok(createJwt(null))
        } else {
            ResponseEntity.status(HttpStatus.GONE).build()

        }
    }

    /**
     * This is called by the device permitting unconfigured device to connect and poll
     */
    open fun createDelegatedEntry(@RequestBody data: DelegatedEntryData): ResponseEntity<String> {
        if (data.pin.length != 8) {
            return ResponseEntity.badRequest().body("Unsupported pin!")
        }
        if (data.requesterId.isBlank() || data.requesterId.length < 6) {
            return ResponseEntity.badRequest().body("Please provide proper id")
        }
        val success = executeWithStatus {
            delegatedAuthenticationTable.insert {
                it[pin] = data.pin
                it[requesterId] = data.requesterId
            }
        }
        return if (success) {
            ResponseEntity.ok().build()
        } else {
            ResponseEntity.unprocessableEntity().build()
        }
    }

    @RestController
    @RequestMapping(path = ["/open"])
    class OpenAuthentication: AuthenticationController() {
        @PostMapping(value = ["/auth/new"])
        override fun createJWT(@RequestBody user: User): Jwt {
            return super.createJWT(user)
        }

        @PostMapping(value = ["/auth/delegate/permit"])
        override fun createDelegatedEntry(@RequestBody data: DelegatedEntryData): ResponseEntity<String> {
            return super.createDelegatedEntry(data)
        }

        @GetMapping(value = ["/auth/delegate/{requesterId}/{pin}/new"])
        override fun createDelegatedJwt(@PathVariable requesterId: String, @PathVariable pin: String): ResponseEntity<Jwt?> {
            return super.createDelegatedJwt(requesterId, pin)
        }
    }

    @RestController
    @RequestMapping(path = ["/secure"])
    class RestrictedAuthentication: AuthenticationController() {

        @PostMapping(value = ["/auth/new"])
        @Authentication(AuthenticationModes.STRICT)
        override fun createJWT(@RequestBody user: User): Jwt {
            return super.createJWT(user)
        }

        @Authentication(AuthenticationModes.STRICT)
        @PostMapping(value = ["/auth/delegate/permit"])
        override fun createDelegatedEntry(@RequestBody data: DelegatedEntryData): ResponseEntity<String> {
            return super.createDelegatedEntry(data)
        }

        @GetMapping(value = ["/auth/delegate/{requesterId}/{pin}/new"])
        override fun createDelegatedJwt(@PathVariable requesterId: String, @PathVariable pin: String): ResponseEntity<Jwt?> {
            return super.createDelegatedJwt(requesterId, pin)
        }



        @Authentication(AuthenticationModes.STRICT)
        @PostMapping(value = ["/auth/new/cast"])
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