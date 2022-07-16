package no.iktdev.streamit.api.controllers.logic

import no.iktdev.streamit.api.Configuration
import no.iktdev.streamit.api.classes.*
import no.iktdev.streamit.api.database.catalog
import no.iktdev.streamit.api.database.movie
import no.iktdev.streamit.api.database.operations.CatalogItemCreateOrUpdate
import no.iktdev.streamit.api.database.serie
import no.iktdev.streamit.api.getContext
import no.iktdev.streamit.api.helper.serieHelper
import no.iktdev.streamit.api.helper.timeParse
import no.iktdev.streamit.api.services.database.CatalogService
import no.iktdev.streamit.api.database.operations.CatalogItemRemovalService
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset

class CatalogLogic {
    companion object {
    }

    class Get {

        fun allItems(): List<Catalog> {
            return transaction {
                catalog.selectAll().mapNotNull { Catalog.fromRow(it) }
            }
        }

        fun allMovies(): List<Catalog> {
            return transaction {
                catalog
                    .selectAll()
                    .andWhere { catalog.iid.greater(0) }
                    .orWhere { catalog.type.eq("movie") }
                    .mapNotNull {
                        Catalog.fromRow(it)
                    }
            }
        }

        fun allSeries(): List<Catalog> {
            return transaction {
                catalog.selectAll()
                    .andWhere { catalog.collection.isNotNull() }
                    .andWhere { catalog.type.eq("serie") }
                    .mapNotNull { Catalog.fromRow(it) }
            }
        }

        fun movieById(id: Int): Movie? {
            return transaction {
                val movie = catalog.innerJoin(movie, { iid }, { movie.id })
                    .select { catalog.id eq id }
                    .andWhere { catalog.iid.isNotNull() }
                    .singleOrNull()
                if (movie != null) Movie.fromRow(movie) else null
            }
        }

        fun serieByCollection(collection: String): Serie? {
            val flatten = transaction {
                catalog.join(serie, JoinType.INNER) { catalog.collection eq serie.collection }
                    .select { catalog.collection.eq(collection) }
                    .andWhere { catalog.collection.isNotNull() }
                    .mapNotNull { SerieFlat.fromRow(it) }
            }
            return serieHelper.map().mergeSerie(flatten)
        }

        fun newContent(): List<Catalog> {
            return transaction {
                catalog
                    .selectAll()
                    .orderBy(catalog.id, SortOrder.DESC)
                    .limit(10)
                    .mapNotNull {
                        Catalog.fromRow(it)
                    }
            }
        }

        fun updatedSeries(): List<Catalog> {
            val zone = ZoneOffset.systemDefault().rules.getOffset(Instant.now())
            val recentAdded = timeParse().recentTime(no.iktdev.streamit.api.Configuration.serieAgeCap)
            val dateTime = LocalDateTime.now().minusDays(no.iktdev.streamit.api.Configuration.frshness)

            return transaction {
                val serieByEpisode = serie.slice(serie.collection, serie.added.max().alias(serie.added.name))
                    .selectAll().groupBy(serie.collection).alias("updatedTable")
                catalog
                    .join(serieByEpisode, JoinType.INNER)
                    {
                        catalog.collection eq serieByEpisode[serie.collection]
                    }
                    .slice(
                        catalog.id,
                        catalog.title,
                        catalog.cover,
                        catalog.type,
                        catalog.collection,
                        catalog.iid,
                        catalog.genres,
                        serieByEpisode[serie.added]
                    )
                    .select { catalog.collection.isNotNull() }
                    .andWhere { catalog.type eq "serie" }
                    // This will
                    .andWhere { serieByEpisode[serie.added].greater(recentAdded.toString()) }
                    .orderBy(serieByEpisode[serie.added], SortOrder.DESC)
                    .mapNotNull {
                        val added = it[serieByEpisode[serie.added]]
                        val recent = added.epochSecond > dateTime.toEpochSecond(zone)
                        Catalog.fromRow(it, recent)
                    }
            }
        }
    }

    class Post {
        companion object {
        }

        fun addSerie(serie: Serie) {
            return CatalogItemCreateOrUpdate().Serie(serie)
        }
        fun addMovie(movie: Movie) {
            return CatalogItemCreateOrUpdate().Movie(movie)
        }

    }

    class Delete {
        companion object {
        }

        fun movieByTitle(title: String): Response {
            return CatalogItemRemovalService().removeMovie(title)
        }
        fun movieById(id: Int): Response {
            return CatalogItemRemovalService().removeMovie(id)
        }

        fun serieByTitle(title: String): Response {
            return CatalogItemRemovalService().removeSerie(title)
        }
        fun serieByCollection(collection: String): Response {
            return CatalogItemRemovalService().removeSerie(collection)
        }
        fun serieById(id: Int): Response {
            return CatalogItemRemovalService().removeSerie(id)
        }
    }
}