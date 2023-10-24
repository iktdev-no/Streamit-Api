package no.iktdev.streamit.api.database
import no.iktdev.streamit.api.Configuration
import no.iktdev.streamit.api.error.MissingConfigurationException
import org.jetbrains.exposed.sql.Database
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
fun timestampToLocalDateTime(timestamp: Int): LocalDateTime {
    return Instant.ofEpochSecond(timestamp.toLong()).atZone(ZoneId.systemDefault()).toLocalDateTime()
}

fun LocalDateTime.toEpochSeconds(): Long {
    return this.toEpochSecond(ZoneOffset.ofTotalSeconds(ZoneOffset.systemDefault().rules.getOffset(LocalDateTime.now()).totalSeconds))
}