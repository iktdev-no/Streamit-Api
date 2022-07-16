package no.iktdev.streamit.api.controllers

import no.iktdev.streamit.api.classes.Catalog
import no.iktdev.streamit.api.classes.Movie
import no.iktdev.streamit.api.classes.Response
import no.iktdev.streamit.api.classes.Serie
import no.iktdev.streamit.api.controllers.logic.CatalogLogic
import no.iktdev.streamit.api.database.operations.CatalogItemCreateOrUpdate
import no.iktdev.streamit.api.database.operations.CatalogItemRemovalService
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping(path = ["/secure"])
class CatalogSecureController {

    @GetMapping("/catalog")
    fun all(): List<Catalog> {
        return CatalogLogic.Get().allItems()
    }

    @GetMapping("/new")
    fun getNewContent(): List<Catalog> {
        return CatalogLogic.Get().newContent()
    }

    @GetMapping("/movie")
    fun allMovies(): List<Catalog> {
        return CatalogLogic.Get().allMovies()
    }

    @GetMapping("/movie/{id}")
    fun movies(@PathVariable id: Int? = -1): Movie? {
        return if (id != null && id > -1) CatalogLogic.Get().movieById(id) else null
    }


    @GetMapping("/serie")
    fun allSeries(): List<Catalog> {
        return CatalogLogic.Get().allSeries()
    }

    @GetMapping("/serie/{collection}")
    fun getSerie(@PathVariable collection: String? = null): Serie? {
        return if (!collection.isNullOrEmpty()) CatalogLogic.Get().serieByCollection(collection) else null
    }

    @GetMapping("/updated")
    fun getUpdatedSeries(): List<Catalog> {
        return CatalogLogic.Get().updatedSeries()
    }

    /**
    *
    * Post Mappings below
    *
    **/


    /*@PostMapping("/serie")
    fun serie(serie: Serie) {
        CatalogItemCreateOrUpdate().Serie(serie)
    }

    @PostMapping("/movie")
    fun movie(movie: Movie) {
        CatalogItemCreateOrUpdate().Movie(movie)
    }*/


    /**
     * Delete Mappings below
     **/

    /*@DeleteMapping("/movie/title")
    fun deleteMovieByTitle(@RequestParam("title") title: String): Response {
        return CatalogItemRemovalService().removeMovie(title)
    }
    @DeleteMapping("/movie/id")
    fun deleteMovieById(@RequestParam("id") id: Int): Response {
        return CatalogItemRemovalService().removeMovie(id)
    }

    @DeleteMapping("/serie/title")
    fun deleteSerieByTitle(@RequestParam("title") title: String): Response {
        return CatalogItemRemovalService().removeSerie(title)
    }
    @DeleteMapping("/serie/collection")
    fun deleteSerieByCollection(@RequestParam("collection") collection: String): Response {
        return CatalogItemRemovalService().removeSerie(collection)
    }
    @DeleteMapping("/serie/id")
    fun deleteSerieById(@RequestParam("id")  id: Int): Response {
        return CatalogItemRemovalService().removeSerie(id)
    }*/

}