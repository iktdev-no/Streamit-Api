package no.iktdev.streamit.api.classes

import no.iktdev.streamit.library.db.tables.subtitle
import org.jetbrains.exposed.sql.ResultRow

data class Subtitle(val id: Int, val associatedWithVideo: String, val language: String, val subtitle: String, val collection: String?, val format: String)
{
    companion object
    {
        fun fromRow(row: ResultRow) = Subtitle(
            id = row[subtitle.id].value,
            associatedWithVideo = row[subtitle.associatedWithVideo],
            language = row[subtitle.language],
            subtitle = row[subtitle.subtitle], // The subtitle file
            collection = row[subtitle.collection],
            format = row[subtitle.format]
        )
    }
}