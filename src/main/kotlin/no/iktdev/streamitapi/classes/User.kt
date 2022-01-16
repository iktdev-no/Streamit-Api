package no.iktdev.streamitapi.classes

import no.iktdev.streamitapi.database.users
import org.jetbrains.exposed.sql.ResultRow

data class User(val guid: String, val name: String, val image: String)
{
    companion object
    {
        fun fromRow(resultRow: ResultRow) = User(
            guid = resultRow[users.guid],
            name = resultRow[users.name],
            image = resultRow[users.image]
        )
    }
}