package no.iktdev.streamit.api

import java.io.File

class Configuration
{
    companion object
    {
        var content: File? = if (!System.getenv("CONTENT_DIRECTORY").isNullOrEmpty()) File(System.getenv("CONTENT_DIRECTORY")) else null

        var frshness: Long = System.getenv("CONTENT_FRESH_DAYS")?.toLong() ?: 5
        var serieAgeCap: String = System.getenv("SERIE_AGE") ?: "30d"
        var continueWatch: Int = System.getenv("CONTENT_CONTINUE")?.toInt() ?: 10
        var jwtSecret: String? = System.getenv("JWT_SECRET") ?: "eO5zESo8livHiDWxwn+J5U7h5cAZPgWZr4JymG94zB0="
        var jwtExpiry: String? = System.getenv("JWT_EXPIRY") ?: null
    }



}