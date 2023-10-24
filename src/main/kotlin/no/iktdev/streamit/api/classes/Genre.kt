package no.iktdev.streamit.api.classes


import no.iktdev.streamit.library.db.tables.genre
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

data class GenreCatalog(val genere: Genre, val catalog: MutableList<Catalog> = mutableListOf())