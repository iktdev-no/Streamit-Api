package no.iktdev.streamitapi.controllers

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


@RestController
class CatalogController
{
    @GetMapping("/catalog")
    fun catalog(): List<Catalog>
    {
        val _catalog: MutableList<Catalog> = mutableListOf()
        val datasource = DataSource().getConnection();
        transaction(datasource) {
            val query: Query = no.iktdev.streamitapi.database.catalog.selectAll()

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
            no.iktdev.streamitapi.database.catalog
                .selectAll()
                .andWhere { no.iktdev.streamitapi.database.catalog.iid.greater(0) }
                .orWhere { no.iktdev.streamitapi.database.catalog.type.eq("movie") }
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
            no.iktdev.streamitapi.database.catalog
                .selectAll()
                .andWhere { no.iktdev.streamitapi.database.catalog.collection.isNotNull() }
                .andWhere { no.iktdev.streamitapi.database.catalog.type.eq("serie") }
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
            val result = no.iktdev.streamitapi.database.catalog.innerJoin(movie, { iid }, { movie.id })
                .select { no.iktdev.streamitapi.database.catalog.id eq id }
                .andWhere { no.iktdev.streamitapi.database.catalog.iid.isNotNull() }
                .singleOrNull()
            _movie = result?.let { Movie.fromRow(it) }
        }
        return _movie
    }

    /**
     * Using Catalog collection to obtain serie
     */
    @GetMapping("/serie/{collection}")
    fun serie(@PathVariable collection: String? = null): Serie?
    {
        var _serie: Serie? = null
        transaction(DataSource().getConnection()) {
            val result = no.iktdev.streamitapi.database.catalog.innerJoin(no.iktdev.streamitapi.database.serie, { no.iktdev.streamitapi.database.catalog.collection }, { no.iktdev.streamitapi.database.serie.collection })
                .select { no.iktdev.streamitapi.database.catalog.collection eq no.iktdev.streamitapi.database.serie.collection}
                .andWhere { no.iktdev.streamitapi.database.catalog.collection.isNotNull() }
                .singleOrNull()

            _serie = result?.let {
                SerieFlat.fromRow(
                    it
                )
            }?.let {
                serieHelper.map().mapFromFlat(
                    it
                )
            }
        }
        return _serie
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