package no.iktdev.streamit.api.database.queries

import no.iktdev.streamit.api.classes.Movie
import no.iktdev.streamit.api.classes.Subtitle
import no.iktdev.streamit.api.helper.withoutExtension
import no.iktdev.streamit.library.db.tables.catalog
import no.iktdev.streamit.library.db.tables.movie
import no.iktdev.streamit.library.db.tables.subtitle
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
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
                .map { Movie.fromRow(it) }.singleOrNull()
            movie?.video?.withoutExtension()?.let { videoName ->
                movie.subs = subtitle.select { subtitle.associatedWithVideo eq videoName }
                    .map { Subtitle.fromRow(it) }
            }
            movie?.genres?.let {
                val ids = it.split(",").mapNotNull { g -> g.toIntOrNull() }
                QGenre().getByIds(ids)
            }
            movie
        }
    }

    fun selectOnTitle(title: String): Movie? {
        return transaction {
            val movie = catalog.innerJoin(movie, { iid }, { movie.id })
                .select { catalog.title eq title }
                .andWhere { catalog.iid.isNotNull() }
                .map { Movie.fromRow(it) }.singleOrNull()
            movie?.video?.withoutExtension()?.let { videoName ->
                movie.subs = subtitle.select { subtitle.associatedWithVideo eq videoName }
                    .map { Subtitle.fromRow(it) }
            }
            movie
        }
    }

    fun deleteMovieItemOn(video: String): Boolean {
        return transaction {
            val rows = movie.deleteWhere { movie.video.eq(video) }
            if (rows > 1) {
                rollback()
                false
            } else
                true
        }
    }

}