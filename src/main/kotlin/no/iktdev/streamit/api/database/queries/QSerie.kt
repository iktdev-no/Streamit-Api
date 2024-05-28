package no.iktdev.streamit.api.database.queries

import no.iktdev.streamit.api.classes.Episode
import no.iktdev.streamit.api.classes.Serie
import no.iktdev.streamit.api.classes.Subtitle
import no.iktdev.streamit.api.helper.withoutExtension
import no.iktdev.streamit.library.db.tables.catalog
import no.iktdev.streamit.library.db.tables.serie
import no.iktdev.streamit.library.db.tables.subtitle
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction

class QSerie {

    fun selectOnId(id: Int): Serie? {
        return transaction {
            val episodeRows = catalog.join(serie, JoinType.INNER) { catalog.collection eq serie.collection }
                .select { catalog.id.eq(id) }
                .andWhere { catalog.type eq "serie" }
                .orderBy(serie.season)
                .orderBy(serie.episode)
                .andWhere { catalog.collection.isNotNull() }
                .andWhere { catalog.type.eq("serie") }
                .filterNotNull()

            val baseSerie = episodeRows.firstOrNull()?.let {
                Serie.basedOn(it).apply {
                    this.episodes = episodes
                }
            }

            val episodes = episodeRows.map {
                Episode(
                    season = it[serie.season],
                    episode = it[serie.episode],
                    title = it[serie.title],
                    video = it[serie.video]
                )
            }

            baseSerie?.let {
                subtitle.select { subtitle.collection eq it.collection }
                    .map { Subtitle.fromRow(it) }.groupBy { it.associatedWithVideo }
                    .forEach { sub ->
                        episodes.firstOrNull { e -> e.video.withoutExtension() == sub.key }?.let {
                            it.subs = sub.value
                        }
                    }
                it.episodes = episodes
            }
            baseSerie
        }
    }

    fun selectOnCollection(collection: String): Serie? {
        return transaction {
            val episodeRows = catalog.join(serie, JoinType.INNER) { catalog.collection eq serie.collection }
                .select { catalog.collection.eq(collection) }
                .orderBy(serie.season)
                .orderBy(serie.episode)
                .andWhere { catalog.collection.isNotNull() }
                .andWhere { catalog.type.eq("serie") }
                .filterNotNull()

            val episodes = episodeRows.map {
                Episode(
                    season = it[serie.season],
                    episode = it[serie.episode],
                    title = it[serie.title],
                    video = it[serie.video]
                )
            }

            subtitle.select { subtitle.collection eq collection }
                .map { Subtitle.fromRow(it) }.groupBy { it.associatedWithVideo }
                .forEach { sub ->
                    episodes.firstOrNull { e -> e.video.withoutExtension() == sub.key }?.let {
                        it.subs = sub.value
                    }
                }
            episodeRows.firstOrNull()?.let {
                Serie.basedOn(it).apply {
                    this.episodes = episodes
                }
            }
        }
    }

    private fun mapToSerie(rows: List<ResultRow>): Serie? {
        val base = Serie.basedOn(rows.first())
        val mapped = rows.map {
            Episode(
                season = it[serie.season],
                episode = it[serie.episode],
                title = it[serie.title],
                video = it[serie.video]
            )
        }
        return base.apply { episodes = mapped }
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