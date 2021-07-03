package no.iktdev.streamitapi.services

import no.iktdev.streamitapi.classes.*
import no.iktdev.streamitapi.database.catalog
import no.iktdev.streamitapi.database.movie
import no.iktdev.streamitapi.database.serie
import no.iktdev.streamitapi.helper.serieHelper
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.statements.InsertStatement
import org.jetbrains.exposed.sql.statements.UpdateStatement
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.stereotype.Service
import java.lang.System.err

@Service
class CatalogService
{
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
                it[catalog.iid] = data.iid
            }
        }

        private fun updateCatalog(data: Catalog): Int {
            return catalog.update( {catalog.title eq data.title} ) {
                it[catalog.title] = data.title
                it[catalog.cover] = data.cover ?: ""
                it[catalog.type] = data.type
                it[catalog.collection] = data.collection ?: ""
                it[catalog.genres] = data.genres ?: ""
                it[catalog.iid] = data.iid
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
                    // Not completed
                    val insert = insertMovie(data)
                    val id = insert.getOrNull(movie.id)
                    if (id != null)
                    {
                        data.iid = id.value;
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

}