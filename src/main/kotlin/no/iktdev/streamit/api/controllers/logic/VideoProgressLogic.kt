package no.iktdev.streamit.api.controllers.logic

import no.iktdev.streamit.api.Configuration
import no.iktdev.streamit.api.classes.BaseProgress
import no.iktdev.streamit.api.classes.ProgressMovie
import no.iktdev.streamit.api.classes.ProgressSerie
import no.iktdev.streamit.api.classes.ProgressTable
import no.iktdev.streamit.api.database.progress
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
            val common = transaction {
                progress.select { progress.guid eq guid }
                    .mapNotNull { ProgressTable.fromRow(it) }
            }
            return progressHelper.map().fromMixedProgressTable(common)
        }

        fun getProgressOnGuidForMovies(guid: String): List<ProgressMovie> {
            return transaction {
                progress.select { progress.guid eq guid }
                    .andWhere { progress.type eq "movie" }
                    .mapNotNull { ProgressMovie.fromRow(it) }
            }
        }

        fun getProgressOnGuidWithMovieTitle(guid: String, title: String): ProgressMovie? {
            val result = transaction {
                progress.select { progress.guid eq guid }
                    .andWhere { progress.title eq title }
                    .andWhere { progress.type eq "movie" }
                    .singleOrNull()

            }
            return if (result != null) ProgressMovie.fromRow(result) else null
        }

        fun getProgressOnGuidForSeries(guid: String): List<ProgressSerie> {
            val result = transaction {
                progress.select { progress.guid eq guid }
                    .andWhere { progress.type eq "serie" }
                    .mapNotNull { ProgressTable.fromRow(it) }
            }
            return if (result.isNotEmpty()) progressHelper.map().fromSerieProgressTable(result) else emptyList()
        }

        fun getProgressOnGuidWithSerieTitle(guid: String, title: String): ProgressSerie? {
            val result = transaction {
                progress.select { progress.guid eq guid }
                    .andWhere { progress.collection eq title }
                    .orWhere { progress.title eq title }
                    .andWhere { progress.type eq "serie" }
                    .mapNotNull { ProgressTable.fromRow(it) }
            }
            return if(result.isNotEmpty()) progressHelper.map().mergeSerieTables(result) else null
        }

        fun getContinueOnForGuid(guid: String): List<BaseProgress> {
            val result = transaction {
                val msx = progress.season.max().alias(progress.season.name)
                val mex = progress.episode.max().alias(progress.episode.name)

                val episodeTable = progress.slice(mex, progress.title).selectAll().groupBy(progress.title).alias("episodeTable")
                val seasonTable = progress.slice(msx, progress.title).selectAll().groupBy(progress.title).alias("seasonTable")

                progress
                    .join(episodeTable, JoinType.INNER) {
                        progress.title eq episodeTable[progress.title] and(progress.episode eq  episodeTable[mex])
                    }
                    .join(seasonTable, JoinType.INNER) {
                        progress.title eq seasonTable[progress.title] and(progress.season eq seasonTable[msx])
                    }
                    .select { progress.played.isNotNull() }
                    .andWhere { progress.guid eq guid }
                    .orderBy(progress.played, SortOrder.DESC)
                    .limit(no.iktdev.streamit.api.Configuration.continueWatch)
                    .mapNotNull {
                        ProgressTable.fromRow(it)
                    }
            }
            return if (result.isNotEmpty()) progressHelper.map().fromMixedProgressTable(result) else emptyList()
        }
    }

    class Post {
        companion object {
            private fun getService(): ProgressService? {
                return getContext()?.getBean(ProgressService::class.java)
            }
        }

        fun updateOrInsertProgressForMovie(@RequestBody progress: ProgressMovie): ResponseEntity<String> {
            getService()?.upsertProgressMovie(progress) ?: return ResponseEntity("Could not obtain progress service", HttpStatus.INTERNAL_SERVER_ERROR)
            return ResponseEntity("Ok", HttpStatus.OK)
        }

        fun updateOrInsertProgressForSerie(@RequestBody progress: ProgressSerie): ResponseEntity<String> {
            ProgressService.validate().serie(progress)
            getService()?.upsertProgressSerie(progress) ?: return ResponseEntity("Could not obtain progress service", HttpStatus.INTERNAL_SERVER_ERROR)
            return ResponseEntity("Ok", HttpStatus.OK)
        }
    }


}