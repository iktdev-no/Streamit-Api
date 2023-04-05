package no.iktdev.streamit.api.controllers

import no.iktdev.streamit.api.classes.Genre
import no.iktdev.streamit.api.controllers.annotations.Authentication
import no.iktdev.streamit.api.controllers.annotations.AuthenticationModes
import no.iktdev.streamit.api.database.queries.QGenre
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

open class GenreController {

    @GetMapping("/genre")
    open fun genres(): List<Genre> {
        return QGenre().selectAll()
    }

    @GetMapping("/genre/{id}")
    open fun genre(@PathVariable id: Int = 0): Genre? {
        return QGenre().selectById(id)
    }

    @RestController
    @RequestMapping(path = ["/open"])
    class Open: GenreController() {}


    @RestController
    @RequestMapping(path = ["/secure"])
    class Secure: GenreController() {

        @Authentication(AuthenticationModes.SOFT)
        override fun genres(): List<Genre> {
            return super.genres()
        }

        @Authentication(AuthenticationModes.SOFT)
        override fun genre(@PathVariable id: Int): Genre? {
            return super.genre(id)
        }
    }

}