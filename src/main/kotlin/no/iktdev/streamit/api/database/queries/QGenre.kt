package no.iktdev.streamit.api.database.queries

import no.iktdev.streamit.api.classes.Genre
import no.iktdev.streamit.library.db.tables.genre
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

class QGenre {
    fun selectAll(): List<Genre> {
        return transaction {
            genre.selectAll().mapNotNull { Genre.fromRow(it) }
        }
    }

    fun selectById(id: Int = -1): Genre? {
        if (id < 0) return null
        val row = transaction {
            genre.select { genre.id.eq(id) }.singleOrNull()
        }
        return if (row == null) null else Genre.fromRow(row)
    }

    fun getByIds(ids: List<Int>): List<Genre> {
        return if (ids.isNotEmpty()) transaction {
            genre.select { genre.id inList ids.toList() }.map { Genre.fromRow(it) }
        } else emptyList()
    }
}