package no.iktdev.streamit.api.database.queries

import no.iktdev.streamit.api.classes.Movie
import no.iktdev.streamit.api.classes.Serie
import no.iktdev.streamit.api.classes.SerieFlat
import no.iktdev.streamit.api.database.catalog
import no.iktdev.streamit.api.database.movie
import no.iktdev.streamit.api.database.serie
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

class QSerie {

    fun selectOnId(id: Int): Serie? {
        val rows = transaction {
            catalog.join(serie, JoinType.INNER) { catalog.collection eq serie.collection }
                .select { catalog.id.eq(id) }
                .andWhere { catalog.type eq "serie" }
                .orderBy(serie.season)
                .orderBy(serie.episode)
                .andWhere { catalog.collection.isNotNull() }
                .mapNotNull { SerieFlat.fromRow(it) }
        }
        return if (rows.isEmpty()) return null else Serie.mapFromFlat(rows)
    }

    fun selectOnCollection(collection: String): Serie? {
        val rows = transaction {
            catalog.join(serie, JoinType.INNER) { catalog.collection eq serie.collection }
                .select { catalog.collection.eq(collection) }
                .orderBy(serie.season)
                .orderBy(serie.episode)
                .andWhere { catalog.collection.isNotNull() }
                .mapNotNull { SerieFlat.fromRow(it) }
        }
        return if (rows.isEmpty()) return null else Serie.mapFromFlat(rows)
    }

    fun deleteEpisodeOnVideo(video: String): Boolean {
        return transaction {
            val rows = serie.deleteWhere { serie.video eq video }
            if (rows > 1) {
                rollback()
                false
            } else
                true
        }
    }
}