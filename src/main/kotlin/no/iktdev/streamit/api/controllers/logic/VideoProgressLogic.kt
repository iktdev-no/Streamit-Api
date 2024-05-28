package no.iktdev.streamit.api.controllers.logic

import no.iktdev.streamit.api.classes.*
import no.iktdev.streamit.api.database.queries.*
import no.iktdev.streamit.api.helper.progressHelper

import kotlin.math.roundToInt

class VideoProgressLogic {

    class Get {
        fun getProgressOnGuid(guid: String): List<BaseProgress> {
            val result = QProgress().selectAllForGuid(guid)
            return progressHelper.map().fromMixedProgressTable(result)
        }

        fun getProgressOnGuidForMovies(guid: String): List<ProgressMovie> {
            val result = QProgress().selectMoviesForGuid(guid)
            return result.map { ProgressMovie.fromProgressTable(it) }
        }

        fun getProgressOnGuidWithMovieTitle(guid: String, title: String): ProgressMovie? {
            val result = QProgress().selectOnMovieTitleForGuid(guid, title)
            return if (result != null) ProgressMovie.fromProgressTable(result) else null
        }

        fun getProgressOnGuidForSeries(guid: String): List<ProgressSerie> {
            val result = QProgress().selectSeriesForGuid(guid)
            return if (result.isNotEmpty()) progressHelper.map().fromSerieProgressTable(result) else emptyList()
        }

        fun getProgressOnGuidWithSerieTitle(guid: String, title: String): ProgressSerie? {
            val result = QProgress().selectOnSerieCollectionForGuid(guid, title)
            return if(result.isNotEmpty()) progressHelper.map().mergeSerieTables(result) else null
        }

        fun getProgressOnGuidSerieAfter(guid: String, time: Int): List<ProgressSerie> {
            val result = QProgress().selectSerieForGuidAfter(guid, time)
            return if (result.isNotEmpty()) progressHelper.map().fromSerieProgressTable(result) else emptyList()
        }

        fun getProgressOnGuidForMovieAfter(guid: String, time: Int): List<ProgressMovie> {
            val result = QProgress().selectMovieForGuidAfter(guid, time)
            return result.map { ProgressMovie.fromProgressTable(it) }
        }


        fun getContinueSerieOnForGuid(guid: String): List<Serie> {
            return QProgress().selectLastEpisodesForGuid(guid).mapNotNull {
                mapToContinueSerie(it).apply {
                    this?.episodes?.map { e -> e.subs = QSubtitle().selectSubtitleForVideo(e.video) }
                }
            }
        }


        private fun mapToContinueSerie(table: ProgressTable): Serie? {
            val catalog = QSerie().selectOnCollection(table.collection)
            return if (catalog == null || table.season == null || table.episode == null) {
                null
            } else {
                val minutesRemaining = ((table.duration - table.progress) / 60000.0 ).roundToInt()

                val episode = if (minutesRemaining > 10) {
                    catalog.episodes.find { it.season == table.season && it.episode == table.episode }?.apply {
                            this.progress = table.progress
                            this.duration = table.duration
                            this.played = table.played
                        }
                } else {
                    catalog.after(table.season, table.episode)
                }
                episode?.let {
                    catalog.copy(episodes = listOf(it))
                }
            }
        }

    }

}