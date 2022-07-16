package no.iktdev.streamit.api.controllers.logic

import no.iktdev.streamit.api.classes.Genre
import no.iktdev.streamit.api.database.genre
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

class GenreLogic {

    fun allGenres(): List<Genre> {
        return transaction {
            genre.selectAll().mapNotNull { Genre.fromRow(it) }
        }
    }

    fun genreById(id: Int = 0): Genre? {
        return transaction {
            val result = genre.select { genre.id.eq(id) }.singleOrNull()
            if (result != null) Genre.fromRow(result) else null
        }
    }


}