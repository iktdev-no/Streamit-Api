package no.iktdev.streamitapi.controllers

import no.iktdev.streamitapi.Configuration
import no.iktdev.streamitapi.database.*
import no.iktdev.streamitapi.classes.Catalog
import no.iktdev.streamitapi.classes.Movie
import no.iktdev.streamitapi.classes.Serie
import no.iktdev.streamitapi.classes.SerieFlat
import no.iktdev.streamitapi.database.DataSource
import no.iktdev.streamitapi.database.movie
import no.iktdev.streamitapi.helper.serieHelper
import no.iktdev.streamitapi.services.CatalogService
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDateTime
import java.time.ZoneOffset


@RestController
class CatalogController
{
    @GetMapping("/catalog")
    fun catalog(): List<Catalog>
    {
        val _catalog: MutableList<Catalog> = mutableListOf()
        val datasource = DataSource().getConnection()
        transaction(datasource) {
            val query: Query = catalog.selectAll()

            query.mapNotNull {
                _catalog.add(Catalog.fromRow(it))
            }
        }
        return _catalog
    }

    @GetMapping("/movie")
    fun allMovies(): List<Catalog>
    {
        val _movies: MutableList<Catalog> = mutableListOf()

        transaction(DataSource().getConnection()) {
            catalog
                .selectAll()
                .andWhere { catalog.iid.greater(0) }
                .orWhere { catalog.type.eq("movie") }
                .mapNotNull {
                    _movies.add(Catalog.fromRow(it))
                }
        }
        return _movies
    }

    @GetMapping("/serie")
    fun allSeries(): List<Catalog>
    {
        val _serie: MutableList<Catalog> = mutableListOf()
        transaction(DataSource().getConnection()) {
            catalog
                .selectAll()
                .andWhere { catalog.collection.isNotNull() }
                .andWhere { catalog.type.eq("serie") }
                .mapNotNull {
                    _serie.add(Catalog.fromRow(it))
                }
        }

        return _serie
    }

    /**
     * Using Catalog id to obtain movie object
     */
    @GetMapping("/movie/{id}")
    fun movies(@PathVariable id: Int? = -1): Movie?
    {
        var _movie: Movie? = null
        if (id == null || id < 0) {
            return _movie
        }
        transaction(DataSource().getConnection()) {
            val result = catalog.innerJoin(movie, { iid }, { movie.id })
                .select { catalog.id eq id }
                .andWhere { catalog.iid.isNotNull() }
                .singleOrNull()
            _movie = result?.let { Movie.fromRow(it) }
        }
        return _movie
    }

    /**
     * Using Catalog collection to obtain serie
     */
    @GetMapping("/serie/{collection}")
    fun getSerie(@PathVariable collection: String? = null): Serie?
    {
        if (collection.isNullOrEmpty()) {
            return null
        }
        var _serie: Serie? = null
        transaction {
            val serieFlat: MutableList<SerieFlat> = mutableListOf()
            catalog
                .join(serie, JoinType.INNER)
                {
                    catalog.collection eq serie.collection
                }
                .select { catalog.collection.eq(collection) }
                .andWhere { catalog.collection.isNotNull() }
                .mapNotNull {
                    serieFlat.add(SerieFlat.fromRow(it))
                }
            _serie = serieHelper.map().mergeSerie(serieFlat)

        }
        return _serie
    }

    @GetMapping("/new")
    fun getNewContent(): List<Catalog>
    {
        val _catalog: MutableList<Catalog> = mutableListOf()
        transaction() {
            catalog
                .selectAll()
                .orderBy(catalog.id, SortOrder.DESC)
                .limit(10)
                .mapNotNull {
                    _catalog.add(Catalog.fromRow(it))
                }
        }
        return _catalog
    }

    @GetMapping("/updated")
    fun getUpdatedSeries(): List<Catalog>
    {
        val dateTime = LocalDateTime.now()
        dateTime.minusDays(Configuration.frshness)

        val updated: MutableList<Catalog> = mutableListOf()
        transaction {
            catalog
                .select { catalog.collection.isNotNull() }
                .andWhere { catalog.type eq "serie" }
                .andWhere { catalog.added.isNotNull() }
                .mapNotNull {
                    val added = it[catalog.added]
                    val recent = added.epochSecond > dateTime.toEpochSecond(ZoneOffset.from(added))
                    updated.add(Catalog.fromRow(it, recent))
                }
        }
        return updated
    }



    /*
    * Post Mappings below
    * */



    @PostMapping("/serie")
    fun serie(serie: Serie)
    {
        CatalogService.InsertOrUpdate().Serie(serie)
    }

    @PostMapping("/movie")
    fun movie(movie: Movie)
    {
        CatalogService.InsertOrUpdate().Movie(movie)
    }


}