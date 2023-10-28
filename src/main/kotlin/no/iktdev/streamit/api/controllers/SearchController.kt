package no.iktdev.streamit.api.controllers

import no.iktdev.streamit.api.classes.Catalog
import no.iktdev.streamit.api.controllers.annotations.Authentication
import no.iktdev.streamit.api.controllers.annotations.AuthenticationModes
import no.iktdev.streamit.api.database.queries.QSearch
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

open class SearchController {

    @GetMapping("/search/movie/{keyword}")
    open fun movieSearch(@PathVariable keyword: String?): List<Catalog> {
        return if (!keyword.isNullOrEmpty()) QSearch().movieOn(keyword) else emptyList()
    }

    @GetMapping("/search/serie/{keyword}")
    open fun serieSearch(@PathVariable keyword: String?): List<Catalog> {
        return if (!keyword.isNullOrEmpty()) QSearch().serieOn(keyword) else emptyList()
    }

    @GetMapping("/search/{keyword}")
    open fun search(@PathVariable keyword: String?): List<Catalog> {
        return if (!keyword.isNullOrEmpty()) QSearch().allOn(keyword) else emptyList()
    }

    @RestController
    @RequestMapping(path = ["/open"])
    class OpenSearch: SearchController()

    @RestController
    @RequestMapping(path = ["/secure"])
    class RestrictedSearch: SearchController() {
        @Authentication(AuthenticationModes.SOFT)
        override fun movieSearch(@PathVariable keyword: String?): List<Catalog> {
            return super.movieSearch(keyword)
        }

        @Authentication(AuthenticationModes.SOFT)
        override fun serieSearch(@PathVariable keyword: String?): List<Catalog> {
            return super.serieSearch(keyword)
        }

        @Authentication(AuthenticationModes.SOFT)
        override fun search(@PathVariable keyword: String?): List<Catalog> {
            return super.search(keyword)
        }
    }

}