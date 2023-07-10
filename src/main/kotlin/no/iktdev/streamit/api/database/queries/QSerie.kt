package no.iktdev.streamit.api.database.queries

import no.iktdev.streamit.api.classes.Serie
import no.iktdev.streamit.api.classes.SerieFlat
import no.iktdev.streamit.library.db.tables.catalog
import no.iktdev.streamit.library.db.tables.serie
import org.jetbrains.exposed.sql.JoinType
import org.jetbrains.exposed.sql.andWhere
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.select
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