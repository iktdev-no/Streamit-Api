package no.iktdev.streamitapi.classes

import no.iktdev.streamitapi.database.subtitle
import org.jetbrains.exposed.sql.ResultRow

data class Subtitle(val id: Int, val title: String, val language: String, val subtitle: String, val collection: String?, val format: String)
{
    companion object
    {
        fun fromRow(row: ResultRow) = Subtitle(
            id = row[subtitle.id].value,
            title = row[subtitle.title],
            language = row[subtitle.language],
            subtitle = row[subtitle.subtitle], // The subtitle file
            collection = row[subtitle.collection],
            format = row[subtitle.format]
        )
    }
}