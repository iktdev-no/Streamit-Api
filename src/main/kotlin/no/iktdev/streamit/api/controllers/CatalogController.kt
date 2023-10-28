package no.iktdev.streamit.api.controllers

import no.iktdev.streamit.api.classes.*
import no.iktdev.streamit.api.controllers.annotations.Authentication
import no.iktdev.streamit.api.controllers.annotations.AuthenticationModes
import no.iktdev.streamit.api.controllers.logic.GenredCatalogLogic
import no.iktdev.streamit.api.database.queries.QCatalog
import no.iktdev.streamit.api.database.queries.QMovie
import no.iktdev.streamit.api.database.queries.QResumeOrNext
import no.iktdev.streamit.api.database.queries.QSerie
import org.springframework.web.bind.annotation.*

open class CatalogController {

    @GetMapping("/catalog")
    open fun all(): List<Catalog> {
        return QCatalog().selectAll()
    }

    @GetMapping("/catalog/new")
    open fun getNewContent(): List<Catalog> {
        return QCatalog().selectRecentlyAdded()
    }

    @GetMapping("/catalog/movie")
    open fun allMovies(): List<Catalog> {
        return QCatalog().selectMovieCatalog()
    }

    @GetMapping("/catalog/movie/{id}")
    open fun movies(@PathVariable id: Int? = -1): Movie? {
        return if (id != null && id > -1) QMovie().selectOnId(id) else null
    }


    @GetMapping("/catalog/serie")
    open fun allSeries(): List<Catalog> {
        return QCatalog().selectSerieCatalog()
    }

    @GetMapping("/catalog/serie/{collection}")
    open fun getSerie(@PathVariable collection: String? = null): Serie? {
        return if (!collection.isNullOrEmpty()) QSerie().selectOnCollection(collection) else null
    }

    @GetMapping("/catalog/updated")
    open fun getUpdatedSeries(): List<Catalog> {
        return QCatalog().selectNewlyUpdatedSerieInCatalog()
    }

    @GetMapping("/catalog/genred")
    open fun getGenredCatalogs(): List<GenreCatalog> {
        return GenredCatalogLogic().getGenreToCatalog()
    }

    @GetMapping("/catalog/{userId}/continue/serie")
    open fun getContinueOrResumeSerie(@PathVariable userId: String): List<Serie> {
        return QResumeOrNext(userId).getResumeOrNextOnSerie()
    }


    @RestController
    @RequestMapping(path = ["/open"])
    class OpenCatalog: CatalogController() {
    }

    @RestController
    @RequestMapping(path = ["/secure"])
    class RestrictedCatalog: CatalogController() {

        @Authentication(AuthenticationModes.SOFT)
        override fun all(): List<Catalog> {
            return super.all()
        }

        @Authentication(AuthenticationModes.SOFT)
        override fun getGenredCatalogs(): List<GenreCatalog> {
            return super.getGenredCatalogs()
        }

        @Authentication(AuthenticationModes.SOFT)
        override fun getNewContent(): List<Catalog> {
            return super.getNewContent()
        }

        @Authentication(AuthenticationModes.SOFT)
        override fun allMovies(): List<Catalog> {
            return super.allMovies()
        }

        @Authentication(AuthenticationModes.SOFT)
        override fun movies(@PathVariable id: Int?): Movie? {
            return super.movies(id)
        }

        @Authentication(AuthenticationModes.SOFT)
        override fun allSeries(): List<Catalog> {
            return super.allSeries()
        }

        @Authentication(AuthenticationModes.SOFT)
        override fun getSerie(@PathVariable collection: String?): Serie? {
            return super.getSerie(collection)
        }

        @Authentication(AuthenticationModes.SOFT)
        override fun getUpdatedSeries(): List<Catalog> {
            return super.getUpdatedSeries()
        }

    }


}