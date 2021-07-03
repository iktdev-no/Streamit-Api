package no.iktdev.streamitapi.controllers

import no.iktdev.streamitapi.classes.Catalog
import no.iktdev.streamitapi.database.catalog
import org.jetbrains.exposed.sql.Query
import org.jetbrains.exposed.sql.andWhere
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController

@RestController
class SearchController
{

    fun sharedSearch(keyword: String?, type: String?): List<Catalog>
    {
        val _result: MutableList<Catalog> = mutableListOf()
        transaction {
            val query: Query = catalog
                .select { catalog.title like "$keyword%" }
            if (type != null)
                query.andWhere { catalog.type eq type }
            query.mapNotNull {
                _result.add(Catalog.fromRow(it))
            }
        }
        return _result
    }

    @GetMapping("/search/movie/{keyword}")
    fun movieSearch(@PathVariable keyword: String?): List<Catalog>
    {
        return sharedSearch(keyword, "movie")
    }

    @GetMapping("/search/serie/{keyword}")
    fun serieSearch(@PathVariable keyword: String?): List<Catalog>
    {
        return sharedSearch(keyword, "serie")
    }

    @GetMapping("/search/{keyword}")
    fun search(@PathVariable keyword: String?): List<Catalog>
    {
        return sharedSearch(keyword, null)
    }
}