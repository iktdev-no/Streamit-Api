package no.iktdev.streamitapi.controllers

import no.iktdev.streamitapi.classes.Genre
import no.iktdev.streamitapi.database.DataSource
import no.iktdev.streamitapi.database.genre
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController

@RestController
class GenreController
{
    @GetMapping("/genre")
    fun genres(): List<Genre>
    {
        val _genres: MutableList<Genre> = mutableListOf()
        transaction(DataSource().getConnection()) {
            genre
                .selectAll()
                .mapNotNull {
                    _genres.add(Genre.fromRow(it))
                }
        }
        return _genres
    }

    @GetMapping("/genre/{id}")
    fun genre(@PathVariable id: Int = 0): Genre?
    {
        var _genre: Genre? = null
        transaction(DataSource().getConnection()) {
            val result = genre.select { genre.id.eq(id) }
                .singleOrNull()
            _genre = result?.let { Genre.fromRow(it) }
        }
        return _genre
    }


}