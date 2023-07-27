package no.iktdev.streamit.api.database.queries

import no.iktdev.streamit.api.Configuration
import no.iktdev.streamit.api.classes.Catalog
import no.iktdev.streamit.api.helper.timeParse
import no.iktdev.streamit.library.db.tables.catalog
import no.iktdev.streamit.library.db.tables.serie
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset

class QCatalog {

    fun selectAll(): List<Catalog> {
        return transaction {
            catalog.selectAll().mapNotNull { Catalog.fromRow(it) }
        }
    }

    fun selectMovieCatalog(): List<Catalog> {
        return transaction {
            catalog.selectAll()
                .andWhere { catalog.type eq "movie" }
                .andWhere { catalog.iid.greater(0) }
                .mapNotNull {
                    Catalog.fromRow(it)
                }
        }
    }


    fun selectSerieCatalogByCollection(collection: String): Catalog? {
        return transaction {
            catalog.select { catalog.collection eq collection }
                .andWhere { catalog.type eq "serie" }
                .mapNotNull {
                    Catalog.fromRow(it)
                }.firstOrNull()
        }
    }

    fun selectSerieCatalog(): List<Catalog> {
        return transaction {
            catalog.selectAll()
                .andWhere { catalog.type eq "serie" }
                .andWhere { catalog.collection.isNotNull() }
                .mapNotNull { Catalog.fromRow(it) }
        }
    }

    fun selectRecentlyAdded(): List<Catalog> {
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

    fun selectNewlyUpdatedSerieInCatalog(): List<Catalog> {
        val recentAdded = timeParse().recentTime(Configuration.serieAgeCap)
        val dateTime = LocalDateTime.now().minusDays(Configuration.frshness)

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
                .andWhere { serieByEpisode[serie.added].greater(recentAdded) }
                .orderBy(serieByEpisode[serie.added], SortOrder.DESC)
                .mapNotNull {
                    val added = it[serieByEpisode[serie.added]]
                    val recent = added > dateTime
                    Catalog.fromRow(it, recent)
                }
        }
    }

    fun deleteCatalogItemOn(id: Int): Boolean {
        return transaction {
            val rows = catalog.deleteWhere { catalog.id eq id }
            if (rows > 1) {
                rollback()
                false
            } else
                true
        }
    }

}