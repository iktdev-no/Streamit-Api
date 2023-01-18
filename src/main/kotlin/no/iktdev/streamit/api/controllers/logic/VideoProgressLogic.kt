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

        private fun mapToContinueSerie(table: ProgressTable): Serie? {
            val catalog = if (table.collection != null) QSerie().selectOnCollection(table.collection) else return null
            return if (catalog == null || table.season == null || table.episode == null) {
                null
            } else {
                catalog.seasons = catalog.seasons.filter { it.season >= table.season }
                val currentSeason = catalog.seasons.find { it.season == table.season } ?: return null
                currentSeason.episodes.removeIf { it.episode < table.episode }
                if (currentSeason.episodes.isEmpty() ||
                    (currentSeason.episodes.size == 1 && currentSeason.episodes.lastOrNull() != null && (currentSeason.episodes.last().duration - 10000) > currentSeason.episodes.last().progress)) {
                    catalog.seasons = catalog.seasons.filter { it.season != currentSeason.season }
                    return catalog
                }

                currentSeason.episodes.find { it.episode == table.episode }.apply {
                    this?.progress = table.progress
                    this?.duration = table.duration
                    this?.played = table.played
                }
                catalog
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