package no.iktdev.streamitapi.services.database

import no.iktdev.streamitapi.classes.*
import no.iktdev.streamitapi.classes.removal.MovieRemovalResult
import no.iktdev.streamitapi.classes.removal.SerieRemovalResult
import no.iktdev.streamitapi.database.catalog
import no.iktdev.streamitapi.database.movie
import no.iktdev.streamitapi.database.serie
import no.iktdev.streamitapi.database.subtitle
import no.iktdev.streamitapi.helper.serieHelper
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.statements.InsertStatement
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.stereotype.Service
import kotlin.math.absoluteValue

@Service
class CatalogService
{
    val fetch = Fetch()
    val removal = Removal()

    class InsertOrUpdate
    {

        private fun isCatalogPresent(data: Catalog): Query {
            return catalog
                .select {catalog.type eq data.type }
                .andWhere { catalog.title eq data.title }
        }

        private fun insertCatalog(data: Catalog): InsertStatement<Number> {
            return catalog.insert {
                it[catalog.title] = data.title
                it[catalog.cover] = data.cover ?: ""
                it[catalog.type] = data.type
                it[catalog.collection] = data.collection ?: ""
                it[catalog.genres] = data.genres ?: ""
            }
        }

        private fun updateCatalog(data: Catalog): Int {
            return catalog.update( {catalog.title eq data.title} ) {
                it[catalog.title] = data.title
                it[catalog.cover] = data.cover ?: ""
                it[catalog.type] = data.type
                it[catalog.collection] = data.collection ?: ""
                it[catalog.genres] = data.genres ?: ""
            }
        }

        fun Catalog(data: Catalog)
        {
            transaction {
                if (isCatalogPresent(data).singleOrNull() == null) {
                    insertCatalog(data)
                }
                else {
                    updateCatalog(data)
                }
            }


        }

        private fun isSeriePresent(item: SerieFlat): Query {
            return serie
                .select { serie.title eq item.title }
                .andWhere { serie.collection eq item.collection }
                .andWhere { serie.episode eq item.episode }
                .andWhere { serie.season eq item.season }
        }

        private fun insertSerie(item: SerieFlat): InsertStatement<Number> {
            return serie.insert {
                it[serie.title] = item.title
                it[serie.collection] = item.collection
                it[serie.season] = item.season
                it[serie.episode] = item.episode
                it[serie.video] = item.video
            }
        }

        private fun updateSerie(id: Int, item: SerieFlat): Int {
            return serie.update( { serie.id eq id } )
            {
                it[serie.title] = item.title
                it[serie.collection] = item.collection
                it[serie.season] = item.season
                it[serie.episode] = item.episode
                it[serie.video] = item.video
            }
        }

        fun Serie(data: Serie)
        {
            Catalog(Serie.toCatalog(data))
            val items = serieHelper.flatten().list(data)
            items.forEach {
                transaction {
                    val present = isSeriePresent(it).singleOrNull()
                    if (present != null) {
                        updateSerie(present[serie.id].value, it)
                    }
                    else {
                        insertSerie(it)
                    }
                }
            }
        }


        private fun isMoviePresent(item: Movie): Query {
            return movie
                .select { movie.video eq item.video }
        }

        private fun insertMovie(item: Movie): InsertStatement<Number>
        {
            return movie.insert {
                it[movie.video] = item.video
            }
        }

        private fun updateMovie(item: Movie): Int {
            return movie.update( { movie.id.eq(item.id) } ) {
                it[movie.video] = item.video
            }
        }

        fun Movie(data: Movie)
        {
            transaction {
                val present = isMoviePresent(data).singleOrNull()
                if (present != null) {
                    updateMovie(data)
                    Catalog(Movie.toCatalog(data))
                }
                else {
                    val insert = insertMovie(data)
                    val id = insert.getOrNull(movie.id)
                    if (id != null)
                    {
                        // data.iid = id.value;
                        Catalog(Movie.toCatalog(data))
                    }
                    else
                    {
                        System.out.println("Failed to get id from inserted movie: $data")
                    }
                }
            }
        }

    }


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