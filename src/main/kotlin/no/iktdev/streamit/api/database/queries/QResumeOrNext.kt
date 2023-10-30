package no.iktdev.streamit.api.database.queries

import kotlinx.coroutines.launch
import no.iktdev.streamit.api.Configuration
import no.iktdev.streamit.api.classes.*
import no.iktdev.streamit.api.database.toEpochSeconds
import no.iktdev.streamit.api.helper.Coroutines
import no.iktdev.streamit.library.db.query.ResumeOrNextQuery
import no.iktdev.streamit.library.db.tables.*
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
        if (isResumable(movie.progress, movie.duration)) {
            ResumeOrNextQuery(
                userId = userId,
                type = movie.type,
                collection = movie.collection,
                video = movie.video
            ).upsertAndGetStatus()
        } else {
            executeWithStatus {
                resumeOrNext.deleteWhere {
                    (resumeOrNext.collection eq movie.collection) and
                            (resumeOrNext.type eq movie.type) and
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
        val filteredOnPlayed = serie.seasons.mapNotNull { sit ->
            val episodes = sit.episodes.filter { eit -> eit.played > 0 }
            if (episodes.isEmpty()) null else sit.copy(sit.season, episodes.toMutableList())
        }

        val latestSeason = filteredOnPlayed.maxByOrNull { it.season }
        val latestEpisode = latestSeason?.episodes?.maxBy { it.episode }
        if (latestEpisode == null || latestEpisode.duration < OneMinute) {
            return
        }
        if (isResumable(latestEpisode.progress, latestEpisode.duration)) {
            ResumeOrNextQuery(
                userId = userId,
                type = serie.type,
                collection = serie.collection,
                episode = latestEpisode.episode,
                season = latestSeason.season,
                video = latestEpisode.video
            ).upsertAndGetStatus()
        } else {
            Coroutines().CoroutineIO().launch {
                val pulledSerie = QSerie().selectOnCollection(serie.collection) ?: return@launch
                val next = pulledSerie.after(latestSeason.season, latestEpisode.episode) ?: return@launch
                val nextSeason = next.first
                val nextEpisode = next.second
                ResumeOrNextQuery(
                    userId = userId,
                    type = serie.type,
                    collection = serie.collection,
                    season = nextSeason,
                    episode = nextEpisode.episode,
                    video = nextEpisode.video
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
        val seasonToEpisode = rows.groupBy { it[serie.season] }.map {
                seasonGroup ->
            val episodes = seasonGroup.value.map {
                Episode(
                    episode = it[serie.episode],
                    title = it[serie.title],
                    video = it[serie.video],
                    progress = it[progress.progress],
                    duration = it[progress.duration],
                    played = it[progress.played]?.toEpochSeconds()?.toInt() ?: 0
                )
            }
            Season<Episode>(season = seasonGroup.key, episodes.toMutableList())
        }
        return base.apply { seasons = seasonToEpisode }
    }

}