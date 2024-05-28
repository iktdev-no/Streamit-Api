package no.iktdev.streamit.api.database.queries

import kotlinx.coroutines.launch
import no.iktdev.streamit.api.Configuration
import no.iktdev.streamit.api.classes.*
import no.iktdev.streamit.api.database.timestampToLocalDateTime
import no.iktdev.streamit.api.database.toEpochSeconds
import no.iktdev.streamit.api.helper.Coroutines
import no.iktdev.streamit.library.db.executeWithStatus
import no.iktdev.streamit.library.db.query.ResumeOrNextQuery
import no.iktdev.streamit.library.db.tables.*
import no.iktdev.streamit.library.db.withTransaction
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import kotlin.math.roundToInt

class QResumeOrNext(val userId: String) {
    companion object {
        val OneMinute = 60000
        fun isResumable(watched: Int, duration: Int): Boolean {
            val minutesRemaining = ((duration - watched) / 60000.0).roundToInt()
            return (minutesRemaining > 10)
        }

    }


    fun setResume(movie: Movie) {
        if (movie.duration < OneMinute) {
            return
        }
        if (isResumable(movie.progress, movie.duration) && movie.played > 0) {
            ResumeOrNextQuery(
                userId = userId,
                type = movie.type.sqlName(),
                collection = movie.collection,
                video = movie.video,
                updated = timestampToLocalDateTime(movie.played)
            ).upsertAndGetStatus()
        } else {
            executeWithStatus {
                resumeOrNext.deleteWhere {
                    (resumeOrNext.collection eq movie.collection) and
                            (resumeOrNext.type eq movie.type.sqlName()) and
                            (resumeOrNext.userId eq userId)
                }
            }
        }
    }

    fun setIgnore(collection: String, type: String, ignore: Boolean = true) {
        executeWithStatus {
            resumeOrNext.update({
                (resumeOrNext.userId eq userId) and
                        (resumeOrNext.collection eq collection) and
                        (resumeOrNext.type eq type)
            }) {
                it[resumeOrNext.ignore] = ignore
            }
        }
    }

    fun setResume(serie: Serie) {
        val filteredOnPlayed  = serie.episodes.filter { it.played > 0 }


        val latestSeason = filteredOnPlayed.maxByOrNull { it.season }?.season
        val latestEpisode = filteredOnPlayed.filter { it.season == latestSeason }.maxByOrNull { it.episode }
        if (latestEpisode == null || latestEpisode.duration < OneMinute) {
            return
        }
        if (isResumable(latestEpisode.progress, latestEpisode.duration)) {
            ResumeOrNextQuery(
                userId = userId,
                type = serie.type,
                collection = serie.collection,
                episode = latestEpisode.episode,
                season = latestEpisode.season,
                video = latestEpisode.video,
                updated = timestampToLocalDateTime(latestEpisode.played)
            ).upsertAndGetStatus()
        } else {
            Coroutines().CoroutineIO().launch {
                val pulledSerie = QSerie().selectOnCollection(serie.collection) ?: return@launch
                val next = pulledSerie.after(latestEpisode.season, latestEpisode.episode) ?: return@launch
                ResumeOrNextQuery(
                    userId = userId,
                    type = serie.type,
                    collection = serie.collection,
                    season = next.episode,
                    episode = next.episode,
                    video = next.video
                ).upsertAndGetStatus()
            }
        }
    }

    fun getResumeOrNextOnSerie(): List<Serie> {
        val joined = withTransaction {
            resumeOrNext
                .join(progress, JoinType.INNER) {
                    progress.episode.eq(resumeOrNext.episode)
                        .and(progress.season.eq(resumeOrNext.season))
                        .and(progress.collection.eq(resumeOrNext.collection))
                        .and(progress.guid.eq(userId))
                }
                .join(serie, JoinType.INNER) {
                    serie.collection.eq(resumeOrNext.collection)
                        .and(serie.episode.eq(resumeOrNext.episode))
                        .and(serie.season.eq(resumeOrNext.season))
                }
                .join(catalog, JoinType.INNER) {
                    catalog.collection.eq(resumeOrNext.collection)
                        .and(catalog.type.eq(resumeOrNext.type))
                }
                .select { resumeOrNext.ignore.neq(true) }
                .andWhere { resumeOrNext.userId.eq(userId) }
                .andWhere { resumeOrNext.type.eq("serie") }
                .orderBy(resumeOrNext.updated, SortOrder.DESC)
                .limit(Configuration.continueWatch)
                .filterNotNull()
        }?.groupBy { it[serie.collection] } ?: emptyMap()
        return joined.mapNotNull { mapToSerie(it.key, it.value) }
    }

    private fun mapToSerie(collection: String, rows: List<ResultRow>): Serie? {
        val base = Serie.basedOn(rows.first())
        val mapped = rows.map {
            Episode(
                season =  it[serie.season],
                episode = it[serie.episode],
                title = it[serie.title],
                video = it[serie.video],
                progress = it[progress.progress],
                duration = it[progress.duration],
                played = it[progress.played]?.toEpochSeconds()?.toInt() ?: 0
            )
        }

        return base.apply { episodes = mapped }
    }

}