package no.iktdev.streamitapi.classes

import no.iktdev.streamitapi.database.genre
import org.jetbrains.exposed.sql.ResultRow

data class Genre(val id: Int, val genre: String)
{
    companion object
    {
        fun fromRow(resultRow: ResultRow) = Genre(
            id = resultRow[genre.id].value,
            genre = resultRow[genre.genre]
        )
    }
}