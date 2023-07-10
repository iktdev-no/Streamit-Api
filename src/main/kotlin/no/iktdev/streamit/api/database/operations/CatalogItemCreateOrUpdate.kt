package no.iktdev.streamit.api.database.operations

import no.iktdev.streamit.api.classes.Catalog
import no.iktdev.streamit.api.classes.Movie
import no.iktdev.streamit.api.classes.Serie
import no.iktdev.streamit.api.classes.SerieFlat
import no.iktdev.streamit.api.helper.serieHelper
import no.iktdev.streamit.library.db.tables.catalog
import no.iktdev.streamit.library.db.tables.movie
import no.iktdev.streamit.library.db.tables.serie
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.statements.InsertStatement
import org.jetbrains.exposed.sql.transactions.transaction

class CatalogItemCreateOrUpdate {
    private fun isCatalogPresent(data: Catalog): Query {
        return catalog
            .select { catalog.type eq data.type }
            .andWhere { catalog.title eq data.title }
    }

    private fun insertCatalog(data: Catalog): InsertStatement<Number> {
        return catalog.insert {
            it[title] = data.title
            it[cover] = data.cover ?: ""
            it[type] = data.type
            it[collection] = data.collection
            it[genres] = data.genres ?: ""
        }
    }

    private fun updateCatalog(data: Catalog): Int {
        return catalog.update( { catalog.title eq data.title} ) {
            it[title] = data.title
            it[cover] = data.cover ?: ""
            it[type] = data.type
            it[collection] = data.collection
            it[genres] = data.genres ?: ""
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
            it[title] = item.title
            it[collection] = item.collection
            it[season] = item.season
            it[episode] = item.episode
            it[video] = item.video
        }
    }

    private fun updateSerie(id: Int, item: SerieFlat): Int {
        return serie.update( { serie.id eq id } )
        {
            it[title] = item.title
            it[collection] = item.collection
            it[season] = item.season
            it[episode] = item.episode
            it[video] = item.video
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
            it[video] = item.video
        }
    }

    private fun updateMovie(item: Movie): Int {
        return movie.update( { movie.id.eq(item.id) } ) {
            it[video] = item.video
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