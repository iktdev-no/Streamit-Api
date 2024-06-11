package no.iktdev.streamit.api.controllers

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.exceptions.JWTVerificationException
import com.auth0.jwt.interfaces.DecodedJWT
import com.fasterxml.jackson.databind.ObjectMapper
import com.google.gson.Gson
import mu.KotlinLogging
import no.iktdev.streamit.api.Configuration
import no.iktdev.streamit.api.classes.Jwt
import no.iktdev.streamit.api.classes.User
import no.iktdev.streamit.api.error.MissingConfigurationException
import no.iktdev.streamit.api.helper.timeParse
import org.springframework.boot.configurationprocessor.json.JSONObject
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder.json
import java.time.Instant
import java.time.ZoneOffset
import java.util.*


open class Authy {
    val log = KotlinLogging.logger {}
     companion object {
         fun algorithm(): Algorithm {
             return Algorithm.HMAC256(Configuration.jwtSecret) ?: throw MissingConfigurationException("HS256 JWT secret is not provided correctly, clear environment variable to use default...")
         }
         val issuer = "StreamIT Instantiated Authy"
     }

    fun createJwt(user: User, ttl: String? = null): Jwt {
        val zone = ZoneOffset.systemDefault().rules.getOffset(Instant.now())
        val usermap = mapOf(
            "guid" to user.guid,
            "name" to user.name,
            "image" to user.image
        )
        val builder = JWT.create()
            .withIssuer(issuer)
            .withIssuedAt(Date.from(Instant.now()))
            .withPayload(mapOf("user" to usermap))
            .withSubject("Authorization for A.O.I.")
        val expiry = if (!ttl.isNullOrEmpty())
        {
            // Overrides configuration
            timeParse().configTime(ttl)
        } else timeParse().configTime(Configuration.jwtExpiry)

        builder.withExpiresAt(Date.from(expiry.toInstant(zone)))

        return Jwt(builder.sign(algorithm()))
    }

    private fun hasBearer(jwt: String): Boolean {
        return jwt.contains("Bearer", true)
    }

    fun decode(jwt: String): DecodedJWT? {
        val strippedBearer = if (hasBearer(jwt)) jwt.substring(jwt.indexOf(" ")+1) else jwt

        val verifier = JWT.require(algorithm()).withIssuer(issuer).build()
        return try {
            verifier.verify(strippedBearer)
        } catch (e: JWTVerificationException) {
            null
        }
    }

    fun isValid(jwt: String?): Boolean {
        if (jwt.isNullOrBlank()) {
            log.error { "Null or Empty JWT passed for validation!" }
            throw RuntimeException("Null or Empty JWT passed!")
        }
        val decoded = decode(jwt) ?: return false
        return !decoded.expiresAt.before(Date())
    }


    fun getUser(jwt: DecodedJWT): User? {
        val claims = jwt.claims
        return if (claims.containsKey("user"))
            Gson().fromJson(claims["user"].toString(), User::class.java)
        else null
    }


}