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

    /**
     * This function is for a remote device to start polling once it received the server configuration
     * It will pull on its own requester id as primary path, then pincode as secondary
     */
    open fun createDelegatedJwt(@PathVariable requesterId: String, @PathVariable pin: String): ResponseEntity<Jwt?> {
        val result = try {
            transaction {
                delegatedAuthenticationTable.select {
                    (delegatedAuthenticationTable.pin eq pin) and
                            (delegatedAuthenticationTable.requesterId eq requesterId)
                }.firstNotNullOfOrNull {
                    InternalDelegatedRequestData(
                        pin = it[delegatedAuthenticationTable.pin],
                        requesterId = it[delegatedAuthenticationTable.requesterId],
                        created = it[delegatedAuthenticationTable.created],
                        expires = it[delegatedAuthenticationTable.expires],
                        consumed = it[delegatedAuthenticationTable.consumed],
                        permitted = it[delegatedAuthenticationTable.permitted],
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return ResponseEntity.internalServerError().build()
        }

        if (result == null) {
            return ResponseEntity.notFound().build()
        }

        return if (result.expires < LocalDateTime.now() || result.consumed) {
            ResponseEntity.status(HttpStatus.GONE).body(null)
        } else if (!result.permitted) {
            ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null)
        } else
            ResponseEntity.ok(createJwt(null))

    }

    fun HttpServletRequest?.getRequestersIp(): String? {
        this ?: return null
        val xforwardedIp: String? = this.getHeader("X-Forwarded-For")
        return if (xforwardedIp.isNullOrEmpty()) {
            this.remoteAddr
        } else xforwardedIp
    }


    /**
     * This is called by the client in order to have a usable entry for the authenticating device to permit with minimal work for the user
     */
    open fun createDelegateRequestEntry(data: DelegatedEntryData, method: AuthMethod, request: HttpServletRequest?): ResponseEntity<DelegatedRequestData>  {
        if (data.pin.isBlank() || data.requesterId.isBlank()) {
            return ResponseEntity.unprocessableEntity().build()
        }
        val ip = request?.getRequestersIp()
        val success = executeWithStatus {
            delegatedAuthenticationTable.insert {
                it[pin] = data.pin
                it[requesterId] = data.requesterId
                it[deviceInfo] = Gson().toJson(data.deviceInfo)
                it[delegatedAuthenticationTable.method] = method
                it[delegatedAuthenticationTable.ipaddress] = ip
            }
        }
        if (!success) {
            return ResponseEntity.unprocessableEntity().build()
        }
        log.info { "Successfully inserted delegate request for ${data.deviceInfo.deviceName?.ifEmpty { data.requesterId }} on $method from $ip\n ${Gson().toJson(data)}" }
        return getPinAndInfo(data.pin)
    }

    open fun permitDelegateRequestEntry(pin: String, authMethod: AuthMethod): ResponseEntity<String> {
        val success = executeWithStatus {
            delegatedAuthenticationTable.update({
                (delegatedAuthenticationTable.method eq authMethod) and
                        (delegatedAuthenticationTable.pin eq pin)
            }) {
                it[permitted] = true
            }
        }
        return if (success) {
            ResponseEntity.ok().build()
        } else {
            ResponseEntity.notFound().build()
        }
    }

    open fun getPinAndInfo(@PathVariable pin: String): ResponseEntity<DelegatedRequestData> {
        val data = withTransaction {
            delegatedAuthenticationTable.select { delegatedAuthenticationTable.pin eq pin }.firstNotNullOfOrNull {
                DelegatedRequestData(
                    pin = it[delegatedAuthenticationTable.pin],
                    requesterId = it[delegatedAuthenticationTable.requesterId],
                    deviceInfo = it[delegatedAuthenticationTable.deviceInfo].let { json ->
                        Gson().fromJson(
                            json,
                            DelegatedDeviceInfo::class.java
                        )
                    },
                    created = it[delegatedAuthenticationTable.created].toEpochSeconds(),
                    expires = it[delegatedAuthenticationTable.expires].toEpochSeconds(),
                    permitted = it[delegatedAuthenticationTable.permitted],
                    consumed = it[delegatedAuthenticationTable.consumed],
                    method = it[delegatedAuthenticationTable.method],
                    ipaddress = it[delegatedAuthenticationTable.ipaddress]
                )
            }
        }
        if (data == null) {
            return ResponseEntity.notFound().build()
        }
        log.info { "Returning ${Gson().toJson(data)}" }
        return ResponseEntity.ok(data);
    }



    @RestController
    @RequestMapping(path = ["/open"])
    class OpenAuthentication: AuthenticationController() {
        @PostMapping(value = ["/auth/new"])
        override fun createJWT(@RequestBody user: User): Jwt {
            return super.createJWT(user)
        }

        @PostMapping(value = ["/auth/delegate/request/qr"])
        fun createDelegateQrRequestEntry(@RequestBody data: DelegatedEntryData, request: HttpServletRequest? = null): ResponseEntity<DelegatedRequestData> {
            return super.createDelegateRequestEntry(data, AuthMethod.QR, request)
        }

        @PostMapping(value = ["/auth/delegate/request/pin"])
        fun createDelegatePinRequestEntry(@RequestBody data: DelegatedEntryData, request: HttpServletRequest? = null): ResponseEntity<DelegatedRequestData> {
            return super.createDelegateRequestEntry(data, AuthMethod.PIN, request)
        }


        @PostMapping(value = ["/auth/delegate/permit/qr"])
        fun permitDelegatedQrEntry(@RequestBody pin: String? = null): ResponseEntity<String> {
            if (pin.isNullOrBlank()) {
                return ResponseEntity.badRequest().build()
            }
            return permitDelegateRequestEntry(pin, AuthMethod.QR)
        }

        @PostMapping(value = ["/auth/delegate/permit/pin"])
        fun permitDelegatedPinEntry(@RequestBody pin: String): ResponseEntity<String> {
            return permitDelegateRequestEntry(pin, AuthMethod.PIN)
        }

        @GetMapping(value = ["/auth/delegate/pending/{pin}"])
        override fun getPinAndInfo(@PathVariable pin: String): ResponseEntity<DelegatedRequestData> {
            return super.getPinAndInfo(pin)
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

        @PostMapping(value = ["/auth/delegate/request/qr"])
        fun createDelegateQrRequestEntry(@RequestBody data: DelegatedEntryData, request: HttpServletRequest? = null): ResponseEntity<DelegatedRequestData> {
            return super.createDelegateRequestEntry(data, AuthMethod.QR, request)
        }

        @PostMapping(value = ["/auth/delegate/request/pin"])
        fun createDelegatePinRequestEntry(@RequestBody data: DelegatedEntryData, request: HttpServletRequest? = null): ResponseEntity<DelegatedRequestData> {
            return super.createDelegateRequestEntry(data, AuthMethod.PIN, request)
        }


        @PostMapping(value = ["/auth/delegate/permit/qr"])
        @Authentication(AuthenticationModes.STRICT)
        fun permitDelegatedQrEntry(@RequestBody pin: String): ResponseEntity<String> {
            return permitDelegateRequestEntry(pin, AuthMethod.QR)
        }

        @PostMapping(value = ["/auth/delegate/permit/pin"])
        @Authentication(AuthenticationModes.STRICT)
        fun permitDelegatedPinEntry(@RequestBody pin: String): ResponseEntity<String> {
            return permitDelegateRequestEntry(pin, AuthMethod.PIN)
        }


        @GetMapping(value = ["/auth/delegate/pending/{pin}"])
        override fun getPinAndInfo(@PathVariable pin: String): ResponseEntity<DelegatedRequestData> {
            return super.getPinAndInfo(pin)
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