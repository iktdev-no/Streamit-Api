package no.iktdev.streamit.api

import java.io.File

class Configuration
{
    companion object
    {
        var address: String? = System.getenv("DATABASE_ADDRESS") ?: "192.168.2.250" // "streamit-db"
        var port: String? = System.getenv("DATABASE_PORT") ?: "8082" //"3306"
        var username: String = System.getenv("DATABASE_USERNAME") ?: "streamit"
        var password: String = System.getenv("DATABASE_PASSWORD") ?: "shFZ27eL2x2NoxyEDBMfDWkvFO"
        var database: String = System.getenv("DATABASE_USE") ?: "streamit"

        var content: File? = if (!System.getenv("CONTENT_DIRECTORY").isNullOrEmpty()) File(System.getenv("CONTENT_DIRECTORY")) else null

        var frshness: Long = System.getenv("CONTENT_FRESH_DAYS")?.toLong() ?: 5
        var serieAgeCap: String = System.getenv("SERIE_AGE") ?: "30d"
        var continueWatch: Int = System.getenv("CONTENT_CONTINUE")?.toInt() ?: 10
        var jwtSecret: String? = System.getenv("JWT_SECRET") ?: "eO5zESo8livHiDWxwn+J5U7h5cAZPgWZr4JymG94zB0="
        var jwtExpiry: String? = System.getenv("JWT_EXPIRY") ?: null
    }



}