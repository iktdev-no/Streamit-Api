package no.iktdev.streamitapi.database

import no.iktdev.streamitapi.error.MissingConfigurationException
import org.jetbrains.exposed.sql.Database

class DataSource
{
    var address: String? = System.getenv("DATABASE_ADDRESS") ?: null
    var port: String? = System.getenv("DATABASE_PORT") ?: null
    var username: String = System.getenv("DATABASE_USERNAME") ?: "streamit"
    var password: String = System.getenv("DATABASE_PASSWORD") ?: "shFZ27eL2x2NoxyEDBMfDWkvFO"

    fun getConnection(): Database {
        if (address == null || port == null) {
            throw MissingConfigurationException("Environment is missing configuration for either Database address or Database port")
        }

        if (!port!!.contains(":")) {
            port = ":$port"
        }
        return Database.connect(
            "jdbc:mysql://$address$port/streamit",
            driver = "com.mysql.jdbc.Driver",
            user = username,
            password = password
        )
    }

}