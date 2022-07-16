package no.iktdev.streamit.api.controllers

import no.iktdev.streamit.api.classes.Genre
import no.iktdev.streamit.api.controllers.annotations.Authentication
import no.iktdev.streamit.api.controllers.annotations.AuthenticationModes
import no.iktdev.streamit.api.controllers.logic.GenreLogic
import no.iktdev.streamit.api.database.DataSource
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(path = ["/secure"])
class GenreSecureController {

    @Authentication(AuthenticationModes.SOFT)
    @GetMapping("/genre")
    fun genres(): List<Genre> {
        return GenreLogic().allGenres()
    }

    @Authentication(AuthenticationModes.SOFT)
    @GetMapping("/genre/{id}")
    fun genre(@PathVariable id: Int = 0): Genre? {
        return GenreLogic().genreById(id)
    }
}