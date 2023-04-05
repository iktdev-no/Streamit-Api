package no.iktdev.streamit.api.controllers

import no.iktdev.streamit.api.classes.Catalog
import no.iktdev.streamit.api.classes.Movie
import no.iktdev.streamit.api.classes.Response
import no.iktdev.streamit.api.classes.Serie
import no.iktdev.streamit.api.controllers.annotations.Authentication
import no.iktdev.streamit.api.controllers.annotations.AuthenticationModes
import no.iktdev.streamit.api.database.operations.CatalogItemCreateOrUpdate
import no.iktdev.streamit.api.database.queries.QCatalog
import no.iktdev.streamit.api.database.queries.QMovie
import no.iktdev.streamit.api.database.queries.QSerie
import no.iktdev.streamit.api.services.content.ContentRemoval
import org.springframework.web.bind.annotation.*

open class CatalogController {

    @GetMapping("/catalog")
    open fun all(): List<Catalog> {
        return QCatalog().selectAll()
    }

    @GetMapping("/new")
    open fun getNewContent(): List<Catalog> {
        return QCatalog().selectRecentlyAdded()
    }

    @GetMapping("/movie")
    open fun allMovies(): List<Catalog> {
        return QCatalog().selectMovieCatalog()
    }

    @GetMapping("/movie/{id}")
    open fun movies(@PathVariable id: Int? = -1): Movie? {
        return if (id != null && id > -1) QMovie().selectOnId(id) else null
    }


    @GetMapping("/serie")
    open fun allSeries(): List<Catalog> {
        return QCatalog().selectSerieCatalog()
    }

    @GetMapping("/serie/{collection}")
    open fun getSerie(@PathVariable collection: String? = null): Serie? {
        return if (!collection.isNullOrEmpty()) QSerie().selectOnCollection(collection) else null
    }

    @GetMapping("/updated")
    open fun getUpdatedSeries(): List<Catalog> {
        return QCatalog().selectNewlyUpdatedSerieInCatalog()
    }

    @RestController
    @RequestMapping(path = ["/open"])
    class Open: CatalogController() {


        /**
         *
         * Post Mappings below
         *
         **/


        @PostMapping("/serie")
        fun serie(serie: Serie) {
            CatalogItemCreateOrUpdate().Serie(serie)
        }

        @PostMapping("/movie")
        fun movie(movie: Movie) {
            CatalogItemCreateOrUpdate().Movie(movie)
        }


        /**
         * Delete Mappings below
         **/

        @DeleteMapping("/movie/title")
        fun deleteMovieByTitle(@RequestParam("title") title: String): Response {
            val movie = QMovie().selectOnTitle(title) ?: return Response(false, "Could not find item on title")
            ContentRemoval.getService()?.removeMovie(movie) ?: return Response(false, "Could not delete item on title")
            return Response(true)
        }
        @DeleteMapping("/movie/id")
        fun deleteMovieById(@RequestParam("id") id: Int): Response {
            val movie = QMovie().selectOnId(id) ?: return Response(false, "Could not find item on title")
            ContentRemoval.getService()?.removeMovie(movie) ?: return Response(false, "Could not delete item on title")
            return Response(true)
        }

        @DeleteMapping("/serie/title")
        fun deleteSerieByTitle(@RequestParam("title") title: String): Response {
            return deleteSerieByCollection(title)
        }
        @DeleteMapping("/serie/collection")
        fun deleteSerieByCollection(@RequestParam("collection") collection: String): Response {
            val serie = QSerie().selectOnCollection(collection) ?: return Response(false, "Could not find item on title")
            ContentRemoval.getService()?.removeSerie(serie) ?: return Response(false, "Could not delete item on title")
            return Response(true)
        }
        @DeleteMapping("/serie/id")
        fun deleteSerieById(@RequestParam("id")  id: Int): Response {
            val serie = QSerie().selectOnId(id) ?: return Response(false, "Could not find item on title")
            ContentRemoval.getService()?.removeSerie(serie) ?: return Response(false, "Could not delete item on title")
            return Response(true)
        }
    }

    @RestController
    @RequestMapping(path = ["/secure"])
    class Secure: CatalogController() {

        @Authentication(AuthenticationModes.SOFT)
        override fun all(): List<Catalog> {
            return super.all()
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