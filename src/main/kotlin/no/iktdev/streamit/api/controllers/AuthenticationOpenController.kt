package no.iktdev.streamit.api.controllers

import com.auth0.jwt.JWT
import no.iktdev.streamit.api.classes.Jwt
import no.iktdev.streamit.api.classes.User
import no.iktdev.streamit.api.helper.timeParse
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.Instant
import java.time.ZoneOffset
import java.util.*

@RestController
@RequestMapping(path = ["/open"])
class AuthenticationOpenController: Authy() {

    @PostMapping(value = ["/auth/new", "/auth/new/aoi"])
    fun createJWT(@RequestBody user: User): Jwt {
        return createJwt(user)
    }
}