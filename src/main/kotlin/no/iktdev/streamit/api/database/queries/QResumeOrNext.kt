package no.iktdev.streamit.api.database.queries

import kotlinx.coroutines.launch
import no.iktdev.streamit.api.classes.Movie
import no.iktdev.streamit.api.classes.Serie
import no.iktdev.streamit.api.helper.Coroutines
import no.iktdev.streamit.library.db.query.ResumeOrNextQuery
import no.iktdev.streamit.library.db.tables.executeWithStatus
import no.iktdev.streamit.library.db.tables.resumeOrNext
import no.iktdev.streamit.library.db.tables.users
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.update
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
            ).insertAndGetStatus()
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
            ).insertAndGetStatus()
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
                ).insertAndGetStatus()
            }

            // Instead of delete, do a query and insert next episode, if last then delete
           /* executeWithStatus {
                resumeOrNext.deleteWhere {
                    (resumeOrNext.collection eq serie.collection) and
                            (resumeOrNext.type eq serie.type) and
                            (resumeOrNext.userId eq userId)
                }
            }*/

        }
    }

    fun getResumeSerie(userId: String, title: String) {
        //val last = QProgress().selectLastEpisodesForGuid(userId)

    }

}