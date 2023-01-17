package no.iktdev.streamit.api.database.queries

import no.iktdev.streamit.api.classes.Movie
import no.iktdev.streamit.api.database.catalog
import no.iktdev.streamit.api.database.movie
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.isNotNull
import org.jetbrains.exposed.sql.andWhere
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.innerJoin
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction

class QMovie {

    fun selectOnId(id: Int): Movie? {
        return transaction {
            val movie = catalog.innerJoin(movie, { iid }, { movie.id })
                .select { catalog.id eq id }
                .andWhere { catalog.iid.isNotNull() }
                .singleOrNull() ?: return@transaction null
            Movie.fromRow(movie)
        }
    }

    fun selectOnTitle(title: String): Movie? {
        return transaction {
            val movie = catalog.innerJoin(movie, { iid }, { movie.id })
                .select { catalog.title eq title }
                .andWhere { catalog.iid.isNotNull() }
                .singleOrNull() ?: return@transaction null
            Movie.fromRow(movie)
        }
    }

    fun deleteMovieItemOn(video: String): Boolean {
        return transaction {
            val rows = movie.deleteWhere { movie.video eq video }
            if (rows > 1) {
                rollback()
                false
            } else
                true
        }
    }

}