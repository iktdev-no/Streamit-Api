package no.iktdev.streamit.api.controllers

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import no.iktdev.streamit.api.Configuration
import no.iktdev.streamit.api.classes.Jwt
import no.iktdev.streamit.api.classes.User
import no.iktdev.streamit.api.controllers.Authy
import no.iktdev.streamit.api.controllers.annotations.Authentication
import no.iktdev.streamit.api.controllers.annotations.AuthenticationModes
import no.iktdev.streamit.api.controllers.logic.UserLogic
import no.iktdev.streamit.api.error.MissingConfigurationException
import no.iktdev.streamit.api.helper.timeParse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.Instant
import java.time.ZoneOffset
import java.util.*
import javax.servlet.http.HttpServletResponse

@RestController
@RequestMapping(path = ["/secure"])
class AuthenticationSecureController: Authy() {

    @Authentication(AuthenticationModes.STRICT)
    @PostMapping(value = ["/auth/new", "/auth/new/aoi"])
    fun createJWT(@RequestBody user: User): Jwt {
        return createJwt(user)
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

        val user = UserLogic.Get().getUserByGuid(payloadUser.guid)
        if (user == null) {
            response.sendError(HttpStatus.BAD_REQUEST.value(), "Unable to find user found in JWT in system")
            return null
        }

        return createJwt(user, "3h")
    }
}