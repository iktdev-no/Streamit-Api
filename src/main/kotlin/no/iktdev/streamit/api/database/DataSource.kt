package no.iktdev.streamit.api.database
import no.iktdev.streamit.api.Configuration
import no.iktdev.streamit.api.error.MissingConfigurationException
import org.jetbrains.exposed.sql.Database
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset

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
fun timestampToLocalDateTime(timestamp: Int): LocalDateTime {
    return Instant.ofEpochSecond(timestamp.toLong()).atZone(ZoneId.systemDefault()).toLocalDateTime()
}

fun LocalDateTime.toEpochSeconds(): Long {
    return this.toEpochSecond(ZoneOffset.of(ZoneOffset.systemDefault().id))
}