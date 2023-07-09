package no.iktdev.streamit.api.classes

import no.iktdev.streamit.library.db.tables.summary
import org.jetbrains.exposed.sql.ResultRow

data class Summary(val id: Int, val description: String, val language: String, val cid: Int)
{
    companion object
    {
        fun fromRow(row: ResultRow) = Summary(
            id = row[summary.id].value,
            description = row[summary.description],
            language = row[summary.language],
            cid = row[summary.cid]
        )
    }
}