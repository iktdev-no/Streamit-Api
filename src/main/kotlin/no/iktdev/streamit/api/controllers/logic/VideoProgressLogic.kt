package no.iktdev.streamit.api.controllers.logic

import no.iktdev.streamit.api.Configuration
import no.iktdev.streamit.api.classes.*
import no.iktdev.streamit.api.database.progress
import no.iktdev.streamit.api.database.queries.QCatalog
import no.iktdev.streamit.api.database.queries.QMovie
import no.iktdev.streamit.api.database.queries.QProgress
import no.iktdev.streamit.api.database.queries.QSerie
import no.iktdev.streamit.api.getContext
import no.iktdev.streamit.api.helper.progressHelper
import no.iktdev.streamit.api.services.database.ProgressService
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RequestBody
import kotlin.math.min
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

        fun getContinueOnForGuid(guid: String): List<BaseCatalog> {
            return getContinueMovieOnGuid(guid) + getContinueSerieOnForGuid(guid)
        }

        fun getContinueMovieOnGuid(guid: String): List<Movie> {
            return QProgress().selectLastMoviesForGuid(guid).mapNotNull {
                mapToContinueMovie(it)
            }
        }

        private fun mapToContinueMovie(table: ProgressTable): Movie? {
            val movieItem = if (table.title != null) QMovie().selectOnTitle(table.title) else null
            return movieItem?.apply {
                progress = table.progress
                duration = table.duration
                played = table.played
            }
        }

        fun getContinueSerieOnForGuid(guid: String): List<Serie> {
            return QProgress().selectLastEpisodesForGuid(guid).mapNotNull {
                mapToContinueSerie(it)
            }
        }

        //private fun apply

        private fun mapToContinueSerie(table: ProgressTable): Serie? {
            val catalog = if (table.collection != null) QSerie().selectOnCollection(table.collection) else return null
            return if (catalog == null || table.season == null || table.episode == null) {
                null
            } else {
                val minutesRemaining = ((table.duration - table.progress) / 60000.0 ).roundToInt()

                val season = if (minutesRemaining > 10) {
                    val episode = catalog
                        .seasons.find { it.season == table.season }
                        ?.episodes?.find { it.episode == table.episode }?.apply {
                            this.progress = table.progress
                            this.duration = table.duration
                            this.played = table.played
                        }
                    if (episode != null) Season(table.season, mutableListOf(episode)) else null
                } else {
                    //val nextInSeason = catalog.seasons.find { it.season == table.season }?.episodes?.firstOrNull { e -> e.episode > table.episode }

                    val withinSeasonWithNextEpisode = catalog.seasons.find { it.season == table.season }?.apply {
                        val firstEpisode = this.episodes.firstOrNull { e -> e.episode > table.episode }
                        this.episodes.removeIf { it != firstEpisode }
                    }

                    val seasonWithNextEpisode = catalog.seasons.firstOrNull { s -> s.season > table.season }?.apply {
                        val firstEpisode = this.episodes.firstOrNull()
                        this.episodes.removeAll {  it != firstEpisode }
                    }

                    if (withinSeasonWithNextEpisode != null && withinSeasonWithNextEpisode.episodes.isNotEmpty()) withinSeasonWithNextEpisode else seasonWithNextEpisode
                }

                if (season != null && season.episodes.isNotEmpty()) {
                    catalog.apply {
                        this.seasons = listOf(season)
                    }
                } else {
                    null
                }

            }
        }

    }

    class Post {
        fun updateOrInsertProgressForMovie(@RequestBody progress: ProgressMovie): ResponseEntity<String> {
            QProgress().upsertMovie(progress)
            //getService()?.upsertProgressMovie(progress) ?: return ResponseEntity("Could not obtain progress service", HttpStatus.INTERNAL_SERVER_ERROR)
            return ResponseEntity("Ok", HttpStatus.OK)
        }

        fun updateOrInsertProgressForSerie(@RequestBody progress: ProgressSerie): ResponseEntity<String> {
            ProgressService.validate().serie(progress)
            QProgress().upsertSerie(progress)
           // getService()?.upsertProgressSerie(progress) ?: return ResponseEntity("Could not obtain progress service", HttpStatus.INTERNAL_SERVER_ERROR)
            return ResponseEntity("Ok", HttpStatus.OK)
        }
    }


}