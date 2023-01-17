package no.iktdev.streamit.api.controllers

import no.iktdev.streamit.api.classes.Catalog
import no.iktdev.streamit.api.classes.Movie
import no.iktdev.streamit.api.classes.Serie
import no.iktdev.streamit.api.controllers.annotations.Authentication
import no.iktdev.streamit.api.controllers.annotations.AuthenticationModes
import no.iktdev.streamit.api.database.queries.QCatalog
import no.iktdev.streamit.api.database.queries.QMovie
import no.iktdev.streamit.api.database.queries.QSerie
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(path = ["/secure"])
class CatalogSecureController {

    @Authentication(AuthenticationModes.SOFT)
    @GetMapping("/catalog")
    fun all(): List<Catalog> {
        return QCatalog().selectAll()
    }

    @Authentication(AuthenticationModes.SOFT)
    @GetMapping("/new")
    fun getNewContent(): List<Catalog> {
        return QCatalog().selectRecentlyAdded()
    }

    @Authentication(AuthenticationModes.SOFT)
    @GetMapping("/movie")
    fun allMovies(): List<Catalog> {
        return QCatalog().selectMovieCatalog()
    }

    @GetMapping("/movie/{id}")
    fun movies(@PathVariable id: Int? = -1): Movie? {
        return if (id != null && id > -1) QMovie().selectOnId(id) else null
    }

    @Authentication(AuthenticationModes.SOFT)
    @GetMapping("/serie")
    fun allSeries(): List<Catalog> {
        return QCatalog().selectSerieCatalog()
    }

    @GetMapping("/serie/{collection}")
    fun getSerie(@PathVariable collection: String? = null): Serie? {
        return if (!collection.isNullOrEmpty()) QSerie().selectOnCollection(collection) else null
    }

    @Authentication(AuthenticationModes.SOFT)
    @GetMapping("/updated")
    fun getUpdatedSeries(): List<Catalog> {
        return QCatalog().selectNewlyUpdatedSerieInCatalog()
    }


}