package no.iktdev.streamitapi.controllers

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import no.iktdev.streamitapi.Configuration
import no.iktdev.streamitapi.classes.Jwt
import no.iktdev.streamitapi.classes.User
import no.iktdev.streamitapi.error.MissingConfigurationException
import no.iktdev.streamitapi.helper.timeParse
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import java.time.Instant
import java.time.ZoneOffset
import java.util.*

@RestController
class AuthController {

    @PostMapping("/auth/new")
    fun createJWT(@RequestBody user: User): Jwt {
        


        val zone = ZoneOffset.systemDefault().rules.getOffset(Instant.now())
        val alg = Algorithm.HMAC256(Configuration.jwtSecret) ?: throw MissingConfigurationException("HS256 JWT secret is not provided correctly, clear environment variable to use default...")

        val builder = JWT.create()
            .withIssuer("streamit system")
            .withIssuedAt(Date.from(Instant.now()))
            .withPayload(mapOf("guid" to user.guid, "name" to user.name))
            .withSubject("authorization O.I.A.")
        val expiry = timeParse().configTime(Configuration.jwtExpiry)
        builder.withExpiresAt(Date.from(expiry.toInstant(zone)))

        return Jwt(builder.sign(alg))
    }


}