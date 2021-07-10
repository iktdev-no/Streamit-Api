package no.iktdev.streamitapi

import no.iktdev.streamitapi.error.MissingConfigurationException
import org.jetbrains.exposed.sql.Database

class Configuration
{
    companion object
    {
        var address: String? = System.getenv("DATABASE_ADDRESS") ?: "streamit-db" //"192.168.2.252"
        var port: String? = System.getenv("DATABASE_PORT") ?: "3306"
        var username: String = System.getenv("DATABASE_USERNAME") ?: "streamit"
        var password: String = System.getenv("DATABASE_PASSWORD") ?: "shFZ27eL2x2NoxyEDBMfDWkvFO"
        var database: String = System.getenv("DATABASE_USE") ?: "streamit"
        var frshness: Long = System.getenv("CONTENT_FRESH_DAYS")?.toLong() ?: 5
        var continueWatch: Int = System.getenv("CONTENT_CONTINUE")?.toInt() ?: 10
    }



}