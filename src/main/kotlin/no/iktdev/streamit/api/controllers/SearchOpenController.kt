package no.iktdev.streamit.api.controllers

import no.iktdev.streamit.api.classes.Catalog
import no.iktdev.streamit.api.controllers.logic.SearchLogic
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(path = ["/open"])
class SearchOpenController {

    @GetMapping("/search/movie/{keyword}")
    fun movieSearch(@PathVariable keyword: String?): List<Catalog> {
        return if (!keyword.isNullOrEmpty()) SearchLogic().movieSearch(keyword) else emptyList()
    }

    @GetMapping("/search/serie/{keyword}")
    fun serieSearch(@PathVariable keyword: String?): List<Catalog> {
        return if (!keyword.isNullOrEmpty()) SearchLogic().serieSearch(keyword) else emptyList()
    }

    @GetMapping("/search/{keyword}")
    fun search(@PathVariable keyword: String?): List<Catalog> {
        return if (!keyword.isNullOrEmpty()) SearchLogic().catalogSearch(keyword) else emptyList()
    }
}