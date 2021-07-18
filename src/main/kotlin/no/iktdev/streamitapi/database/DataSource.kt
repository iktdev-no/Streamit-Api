package no.iktdev.streamitapi.database

import no.iktdev.streamitapi.Configuration
import no.iktdev.streamitapi.error.MissingConfigurationException
import org.jetbrains.exposed.sql.Database

class DataSource
{
    fun getConnection(): Database {
        if (Configuration.address == null || Configuration.port == null) {
            throw MissingConfigurationException("Environment is missing configuration for either Database address or Database port")
        }
        var address = Configuration.address
        val database = Configuration.database
        if (!Configuration.port!!.contains(":")) {
            address += ":" + Configuration.port
        }



        return Database.connect(
            "jdbc:mysql://$address/$database",
            //driver = "com.mysql.jdbc.Driver",
            user = Configuration.username,
            password = Configuration.password
        )
    }

}