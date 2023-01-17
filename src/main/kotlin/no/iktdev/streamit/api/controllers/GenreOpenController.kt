package no.iktdev.streamit.api.controllers

import no.iktdev.streamit.api.classes.Genre
import no.iktdev.streamit.api.database.queries.QGenre
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(path = ["/open"])
class GenreOpenController {

    @GetMapping("/genre")
    fun genres(): List<Genre> {
        return QGenre().selectAll()
    }

    @GetMapping("/genre/{id}")
    fun genre(@PathVariable id: Int = 0): Genre? {
        return QGenre().selectById(id)
    }
}