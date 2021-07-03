package no.iktdev.streamitapi.classes

import no.iktdev.streamitapi.database.profiles
import org.jetbrains.exposed.sql.ResultRow

data class Profile(val guid: String, val username: String, val image: String)
{
    companion object
    {
        fun fromRow(resultRow: ResultRow) = Profile(
            guid = resultRow[profiles.guid],
            username = resultRow[profiles.username],
            image = resultRow[profiles.image]
        )
    }
}