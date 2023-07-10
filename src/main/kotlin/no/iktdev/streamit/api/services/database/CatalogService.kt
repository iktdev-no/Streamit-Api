package no.iktdev.streamit.api.services.database

import no.iktdev.streamit.api.classes.Catalog
import no.iktdev.streamit.api.classes.removal.MovieRemovalResult
import no.iktdev.streamit.api.classes.removal.SerieRemovalResult
import no.iktdev.streamit.library.db.tables.catalog
import no.iktdev.streamit.library.db.tables.movie
import no.iktdev.streamit.library.db.tables.serie
import no.iktdev.streamit.library.db.tables.subtitle
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.stereotype.Service
import kotlin.math.absoluteValue

@Service
class CatalogService
{
    val fetch = Fetch()
    val removal = Removal()



    class Fetch {
        fun getMovieFile(id: Int): String? {
            val result = transaction {
                val resultRow = movie.select {movie.id eq id}.singleOrNull()
                if (resultRow != null) resultRow[movie.video] else null
            }
            return result
        }

        fun getMovieFile(title: String): String? {
            val result = transaction {
                val catalogItem = catalog.select { catalog.title eq title }.singleOrNull()
                val iid = catalogItem?.getOrNull(catalog.iid)?.absoluteValue
                val resultRow = movie.select {movie.id eq iid}.singleOrNull()
                if (resultRow != null) resultRow[movie.video] else null
            }
            return result
        }

        fun getSerieCollection(id: Int): String? {
            val result = transaction {
                val resultRow = catalog.select { catalog.id eq id }.singleOrNull()
                if (resultRow != null) resultRow[catalog.collection] else null
            }
            return result
        }
    }

    class Removal {
        fun removeMovie(fileName: String): MovieRemovalResult? {
            val iid = transaction {
                val movieRecord = movie.select { movie.video eq fileName }.singleOrNull()
                if (movieRecord != null) movieRecord[movie.id].value else null
            } ?: return null

            val catalogItem = transaction {
                val ci = catalog.select { catalog.iid eq iid }.singleOrNull()
                if (ci != null) Catalog.fromRow(ci) else null
            } ?: return null

            val deletion = transaction {
               try {
                   val subtitleDeletion = subtitle.deleteWhere { subtitle.title eq catalogItem.title }
                   val catalogDeleted = catalog.deleteWhere { catalog.iid eq iid }
                   val movieDeleted = movie.deleteWhere { movie.id eq iid }

                   if (movieDeleted == 0 || movieDeleted > 1) {
                       rollback()
                   }

                   MovieRemovalResult(
                       countCatalog = catalogDeleted,
                       countMovie = movieDeleted,
                       countSubtitle = subtitleDeletion
                   )
               }
               catch (e: Exception) {
                   e.printStackTrace()
                   rollback()
                   null // <-- result
               }
            }
            return deletion
        }

        fun removeSerie(collection: String): SerieRemovalResult? {
            val catalogItem = transaction {
                val ci = catalog.select { catalog.collection eq collection }.singleOrNull()
                if (ci != null) Catalog.fromRow(ci) else null
            } ?: return null
            if (catalogItem.collection.isNullOrEmpty()) {
                return null
            }
            val deletion = transaction {
                try {
                    val episodeDeletion = serie.deleteWhere { serie.collection eq collection }
                    val catalogDeletion = catalog.deleteWhere { catalog.id eq catalogItem.id }
                    val subtitleDeletion = catalog.deleteWhere { subtitle.collection eq collection }

                    if (catalogDeletion > 1 || episodeDeletion == 0) {
                        rollback()
                    }

                    SerieRemovalResult(
                        countCatalog = catalogDeletion,
                        countEpisode = episodeDeletion,
                        countSubtitle = subtitleDeletion
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                    rollback()
                    null // <-- result
                }
            }
            return deletion
        }

        fun removeEpisode(fileName: String): SerieRemovalResult? {
            val baseFileName = fileName.substring(0, fileName.lastIndexOf("."))

            val episodeItemRecord = transaction {
                serie.select { serie.video eq fileName }.singleOrNull()
            } ?: return null
            val collection = episodeItemRecord[serie.collection]



            val deletion = transaction {
                try {
                    val episodeDeletion = serie.deleteWhere { serie.video eq fileName }

                    val episodeCount = serie.select { serie.collection eq collection }.count()
                    val catalogDeletion = if (episodeCount == 0L) {
                        catalog.deleteWhere { catalog.collection eq collection }
                    } else 0
                    val subtitleDeletion = subtitle.deleteWhere { subtitle.title eq baseFileName }

                    if (episodeDeletion == 0) {
                        rollback()
                        return@transaction null
                    }

                    SerieRemovalResult(
                        countCatalog = catalogDeletion,
                        countEpisode = episodeDeletion,
                        countSubtitle = subtitleDeletion
                    )
                }
                catch (e: Exception) {
                    e.printStackTrace()
                    rollback()
                    null
                }
            }
            return deletion
        }
    }

}