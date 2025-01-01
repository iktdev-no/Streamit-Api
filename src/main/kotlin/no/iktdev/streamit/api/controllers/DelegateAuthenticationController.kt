package no.iktdev.streamit.api.controllers

import com.google.gson.Gson
import no.iktdev.streamit.api.classes.Jwt
import no.iktdev.streamit.api.classes.remote.*
import no.iktdev.streamit.api.controllers.annotations.Authentication
import no.iktdev.streamit.api.controllers.annotations.AuthenticationModes
import no.iktdev.streamit.api.database.toEpochSeconds
import no.iktdev.streamit.api.getRequestersIp
import no.iktdev.streamit.library.db.executeWithStatus
import no.iktdev.streamit.library.db.tables.delegatedAuthenticationTable
import no.iktdev.streamit.library.db.withTransaction
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDateTime
import javax.servlet.http.HttpServletRequest

open class DelegateAuthenticationController: Authy() {

    /**
     *
     */
    fun createDelegationRequestSession(data: AuthInitiateRequest, pinOrQr: String, request: HttpServletRequest?): ResponseEntity<RequestCreatedResponse> {
        val ip = request?.getRequestersIp()
        val reqId = data.toRequestId()
        var insertedId: Int? = null
        val success = executeWithStatus {
            insertedId = delegatedAuthenticationTable.insertAndGetId {
                it[pin] = data.pin
                it[requesterId] = reqId
                it[deviceInfo] = Gson().toJson(data.deviceInfo)
                it[method] = method
                it[ipaddress] = ip
            }.value
        }
        if (!success) {
            return ResponseEntity.unprocessableEntity().build()
        }
        val expires = withTransaction {
            delegatedAuthenticationTable.select {
                delegatedAuthenticationTable.id eq insertedId
            }.map { it[delegatedAuthenticationTable.expires] }.firstOrNull()
        }
        log.info { "Successfully inserted delegate request for ${data.deviceInfo.name.ifEmpty { reqId }} on $pinOrQr from $ip\n ${Gson().toJson(data)}" }
        if (expires == null) {
            log.error { "Expiry is null!" }
        }
        return ResponseEntity.ok(RequestCreatedResponse(
            expiry = expires?.toEpochSeconds() ?: 0,
            sessionId = reqId
        ))
    }

    fun getPendingRequestSessionsWithPIN(pin: String): ResponseEntity<DelegatedRequestData> {
        val data = withTransaction {
            delegatedAuthenticationTable.select { delegatedAuthenticationTable.pin eq pin }.firstNotNullOfOrNull {
                DelegatedRequestData(
                    pin = it[delegatedAuthenticationTable.pin],
                    requesterId = it[delegatedAuthenticationTable.requesterId],
                    deviceInfo = it[delegatedAuthenticationTable.deviceInfo].let { json ->
                        Gson().fromJson(
                            json,
                            RequestDeviceInfo::class.java
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

    open fun permitDelegateRequestEntry(pin: String, session: String): ResponseEntity<String> {
        val success = executeWithStatus {
            delegatedAuthenticationTable.update({
                (delegatedAuthenticationTable.requesterId eq session) and
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

    open fun createDelegatedJwt(@PathVariable session: String, @PathVariable pin: String, request: HttpServletRequest? = null): ResponseEntity<out Jwt?>? {
        val result = try {
            transaction {
                delegatedAuthenticationTable.select {
                    (delegatedAuthenticationTable.pin eq pin) and
                            (delegatedAuthenticationTable.requesterId eq session)
                }.firstNotNullOfOrNull {
                    InternalDelegatedRequestData(
                        pin = it[delegatedAuthenticationTable.pin],
                        requesterId = it[delegatedAuthenticationTable.requesterId],
                        created = it[delegatedAuthenticationTable.created],
                        expires = it[delegatedAuthenticationTable.expires],
                        consumed = it[delegatedAuthenticationTable.consumed],
                        permitted = it[delegatedAuthenticationTable.permitted],
                        ipaddress = it[delegatedAuthenticationTable.ipaddress]
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

        if (request.getRequestersIp() != result.ipaddress) {
            return ResponseEntity.status(409).build()
        }

        log.info { "Consuming authorization on pin: ${result.pin} requested by ${request.getRequestersIp()}" }


        return if (result.expires < LocalDateTime.now() || result.consumed) {
            if (result.consumed) {
                log.info { "Authorization is already consumed" }
            } else {
                log.info { "Authorization is expired.." }
            }
            ResponseEntity.status(HttpStatus.GONE).body(null)
        } else if (!result.permitted) {
            log.info { "Authorization needs to be granted.." }
            ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null)
        } else {
            result.let {  consumable ->
                transaction {
                    delegatedAuthenticationTable.update({
                        (delegatedAuthenticationTable.requesterId eq consumable.requesterId) and
                                (delegatedAuthenticationTable.pin eq consumable.pin)
                    }) {
                        it[consumed] = true
                    }
                }
            }
            ResponseEntity.ok(createJwt(null))
        }
    }


    @RestController
    @RequestMapping(path = ["/open/delegate"])
    class OpenDelegateAuthenticationController: DelegateAuthenticationController() {

        @GetMapping(value = ["/required"])
        fun doesRequireDelegate(): ResponseEntity<Boolean> {
            return ResponseEntity.ok(false)
        }

        /**
         * Creates a delegation request session for PIN-based authentication.
         *
         * This endpoint accepts an {@link AuthInitiateRequest} object in the request body
         * and initiates a delegation session specifically for "PIN" authentication.
         *
         * @param data The authentication initiation request containing necessary details.
         * @param request (Optional) The HTTP servlet request for context information.
         * @return A {@link ResponseEntity} containing a session identifier as a string.
         */
        @PostMapping(value = ["/request/pin"])
        fun createPINDelegationRequestSession(@RequestBody data: AuthInitiateRequest, request: HttpServletRequest? = null): ResponseEntity<RequestCreatedResponse> {
            return createDelegationRequestSession(data, "PIN", request)
        }

        /**
         * Creates a delegation request session for QR code-based authentication.
         *
         * This endpoint accepts an {@link AuthInitiateRequest} object in the request body
         * and initiates a delegation session specifically for "QR" authentication.
         *
         * @param data The authentication initiation request containing necessary details.
         * @param request (Optional) The HTTP servlet request for context information.
         * @return A {@link ResponseEntity} containing a session identifier as a string.
         */
        @PostMapping(value = ["/request/qr"])
        fun createQRDelegationRequestSession(@RequestBody data: AuthInitiateRequest, request: HttpServletRequest? = null): ResponseEntity<RequestCreatedResponse> {
            return createDelegationRequestSession(data, "QR", request)
        }

        @GetMapping(value = ["/request/pending/{session}/{pin}"])
        fun requestDelegationOnSession(@PathVariable session: String, @PathVariable pin: String, request: HttpServletRequest?): ResponseEntity<out Jwt?>? {
            return super.createDelegatedJwt(session, pin, request)
        }

        @GetMapping(value = ["/request/pending/{pin}"])
        fun getPendingRequestOnPIN(@PathVariable pin: String): ResponseEntity<DelegatedRequestData> {
            return super.getPendingRequestSessionsWithPIN(pin)
        }



        @PostMapping(value = ["/permit/request/{session}/{pin}"])
        fun permitDelegationRequest(@RequestBody permitData: PermitRequestData, @PathVariable session: String, @PathVariable pin: String): ResponseEntity<String> {
            return super.permitDelegateRequestEntry(pin = pin, session = session)
        }
    }



    @RestController
    @RequestMapping(path = ["/secure/delegate"])
    class RestrictedDelegateAuthenticationController: DelegateAuthenticationController() {

        @GetMapping(value = ["/required"])
        @Authentication(AuthenticationModes.NONE)
        fun doesRequireDelegate(): ResponseEntity<Boolean> {
            return ResponseEntity.ok(true)
        }

        /**
         * Creates a delegation request session for PIN-based authentication.
         *
         * This endpoint accepts an {@link AuthInitiateRequest} object in the request body
         * and initiates a delegation session specifically for "PIN" authentication.
         *
         * @param data The authentication initiation request containing necessary details.
         * @param request (Optional) The HTTP servlet request for context information.
         * @return A {@link ResponseEntity} containing a session identifier as a string.
         */
        @PostMapping(value = ["/request/pin"])
        @Authentication(AuthenticationModes.NONE)
        fun createPINDelegationRequestSession(@RequestBody data: AuthInitiateRequest, request: HttpServletRequest? = null): ResponseEntity<RequestCreatedResponse> {
            return createDelegationRequestSession(data, "PIN", request)
        }

        /**
         * Creates a delegation request session for QR code-based authentication.
         *
         * This endpoint accepts an {@link AuthInitiateRequest} object in the request body
         * and initiates a delegation session specifically for "QR" authentication.
         *
         * @param data The authentication initiation request containing necessary details.
         * @param request (Optional) The HTTP servlet request for context information.
         * @return A {@link ResponseEntity} containing a session identifier as a string.
         */
        @PostMapping(value = ["/request/qr"])
        @Authentication(AuthenticationModes.NONE)
        fun createQRDelegationRequestSession(@RequestBody data: AuthInitiateRequest, request: HttpServletRequest? = null): ResponseEntity<RequestCreatedResponse> {
            return createDelegationRequestSession(data, "QR", request)
        }

        @GetMapping(value = ["/request/pending/{session}/{pin}"])
        @Authentication(AuthenticationModes.NONE)
        fun requestDelegationOnSession(@PathVariable session: String, @PathVariable pin: String, request: HttpServletRequest?): ResponseEntity<out Jwt?>? {
            return super.createDelegatedJwt(session, pin, request)
        }

        @GetMapping(value = ["/request/pending/{pin}"])
        @Authentication(AuthenticationModes.STRICT)
        fun getPendingRequestOnPIN(@PathVariable pin: String): ResponseEntity<DelegatedRequestData> {
            return super.getPendingRequestSessionsWithPIN(pin)
        }



        @PostMapping(value = ["/permit/request/{session}/{pin}"])
        @Authentication(AuthenticationModes.STRICT)
        fun permitDelegationRequest(@RequestBody permitData: PermitRequestData, @PathVariable session: String, @PathVariable pin: String): ResponseEntity<String> {
            return super.permitDelegateRequestEntry(pin = pin, session = session)
        }





    }


}