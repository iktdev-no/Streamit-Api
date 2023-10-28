package no.iktdev.streamit.api.database.queries

import no.iktdev.streamit.api.classes.Episode
import no.iktdev.streamit.api.classes.Season
import no.iktdev.streamit.api.classes.Serie
import no.iktdev.streamit.library.db.tables.catalog
import no.iktdev.streamit.library.db.tables.serie
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
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
                .andWhere { catalog.type.eq("serie") }
                .filterNotNull()
        }
        val collection = rows.firstOrNull()?.getOrNull(serie.collection) ?: return null
        return if (rows.isEmpty()) return null else mapToSerie(collection, rows)
    }

    fun selectOnCollection(collection: String): Serie? {
        val rows = transaction {
            catalog.join(serie, JoinType.INNER) { catalog.collection eq serie.collection }
                .select { catalog.collection.eq(collection) }
                .orderBy(serie.season)
                .orderBy(serie.episode)
                .andWhere { catalog.collection.isNotNull() }
                .andWhere { catalog.type.eq("serie") }
                .filterNotNull()
        }
        return if (rows.isEmpty()) return null else mapToSerie(collection, rows)
    }

    private fun mapToSerie(collection: String, rows: List<ResultRow>): Serie? {
        val base = Serie.basedOn(rows.first())
        val seasonToEpisode = rows.groupBy { it[serie.season] }.map {
                seasonGroup ->
                val episodes = seasonGroup.value.map {
                    Episode(
                        episode = it[serie.episode],
                        title = it[serie.title],
                        video = it[serie.video]
                    )
                }
             Season<Episode>(season = seasonGroup.key, episodes.toMutableList())
        }
        return base.apply { seasons = seasonToEpisode }
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