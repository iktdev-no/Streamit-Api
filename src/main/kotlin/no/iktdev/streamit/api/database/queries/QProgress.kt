package no.iktdev.streamit.api.database.queries

import no.iktdev.streamit.api.Configuration
import no.iktdev.streamit.api.Log
import no.iktdev.streamit.api.classes.BaseProgress
import no.iktdev.streamit.api.classes.ProgressMovie
import no.iktdev.streamit.api.classes.ProgressSerie
import no.iktdev.streamit.api.classes.ProgressTable
import no.iktdev.streamit.api.database.movie
import no.iktdev.streamit.api.database.progress
import no.iktdev.streamit.api.helper.progressHelper
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.isNotNull
import org.jetbrains.exposed.sql.transactions.transaction

/**
 * Query Class for Progress data
 * Only simple transformations will be performed
 * Other results will be in mapped format equivalent to database table
 */
class QProgress {

    fun selectAllForGuid(guid: String): List<ProgressTable> {
        return transaction {
            progress.select { progress.guid eq guid }
                .mapNotNull { ProgressTable.fromRow(it) }
        }
    }

    fun selectMoviesForGuid(guid: String): List<ProgressTable> {
        return transaction {
            progress.select { progress.guid eq guid }
                .andWhere { progress.type eq "movie" }
                .mapNotNull { ProgressTable.fromRow(it) }
        }
    }

    fun selectSeriesForGuid(guid: String): List<ProgressTable> {
        return transaction {
            progress.select { progress.guid eq guid }
                .andWhere { progress.type eq "serie" }
                .mapNotNull { ProgressTable.fromRow(it) }
        }
    }

    fun selectOnMovieTitleForGuid(guid: String, title: String): ProgressTable? {
        val result = transaction {
            progress.select { progress.guid eq guid }
                .andWhere { progress.title eq title }
                .andWhere { progress.type eq "movie" }
                .singleOrNull()
        }
        return if (result != null) ProgressTable.fromRow(result) else null
    }

    fun selectOnSerieCollectionForGuid(guid: String, collection: String): List<ProgressTable> {
        return transaction {
            progress.select { progress.guid eq guid }
                .andWhere { progress.collection eq collection }
                .orWhere { progress.title eq collection }
                .andWhere { progress.type eq "serie" }
                .mapNotNull { ProgressTable.fromRow(it) }
        }
    }

    fun selectLastMoviesForGuid(guid: String): List<ProgressTable> {
        return transaction {
            progress
                .select { progress.played.isNotNull() and progress.played.greater(0) }
                .andWhere { progress.guid eq guid }
                .andWhere { progress.type eq "movie" }
                .orderBy(progress.played, SortOrder.DESC)
                .limit(Configuration.continueWatch)
                .mapNotNull {
                    ProgressTable.fromRow(it)
                }
        }
    }


    fun selectLastEpisodesForGuid(guid: String): List<ProgressTable> {
        return transaction {
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
                .andWhere { progress.type eq "serie" }
                .orderBy(progress.played, SortOrder.DESC)
                .limit(Configuration.continueWatch)
                .mapNotNull {
                    ProgressTable.fromRow(it)
                }
        }
    }

    fun upsertMovie(movie: ProgressMovie) {
        if (movie.duration == 0) {
            Log(this::class.java ).error("${movie.title}: Duration is 0")
        } else if (movie.video.isNullOrEmpty()) {
            Log(this::class.java ).error("${movie.title}: Video is null or empty")
        }
        if (movie.played <= 0)
            movie.played = (System.currentTimeMillis() / 1000L).toInt();

        transaction {
            val presentRecord = selectMovieRecordOnGuidAndTitle(movie.guid, movie.title)
            if (presentRecord != null) {
                progress.update({ progress.id eq presentRecord[progress.id] })
                {
                    it[this.progress] = movie.progress
                    it[this.duration] = movie.duration
                    it[this.played] = movie.played
                    if (movie.video?.isNotBlank() == true) {
                        it[this.video] = movie.video
                    }
                }
            } else {
                progress.insert {
                    it[this.guid] = movie.guid
                    it[this.type] = movie.type
                    it[this.title] = movie.title.trim()
                    it[this.progress] = movie.progress
                    it[this.duration] = movie.duration
                    it[this.played] = movie.played
                    if (movie.video?.isNotBlank() == true) {
                        it[this.video] = movie.video
                    }
                }
            }
        }
    }
    private fun selectMovieRecordOnGuidAndTitle(guid: String, title: String): ResultRow? {
        return transaction {
            progress
                .select { progress.guid eq guid }
                .andWhere { progress.title eq title.trim() }
                .andWhere { progress.type eq "movie" }
                .singleOrNull()
        }
    }

    fun upsertSerie(serie: ProgressSerie) {
        val mapped = serieToProgressTable(serie)
        if (mapped.any { it.duration <= 0 }) {
            Log(this::class.java).error("$serie")
        }
        mapped.forEach { entry ->
            val record = selectSerieRecordOnGuidAndCombinationValues(entry)
            transaction {
                if (record != null) {
                    progress.update({ progress.id eq record[progress.id] }) { table ->
                        table[this.video] = video
                        table[this.progress] = entry.progress
                        table[this.duration] = entry.duration
                        table[this.played] = entry.played
                        table[this.title] = title
                        if (entry.video?.isNotBlank() == true) {
                            table[this.video] = entry.video
                        }
                    }
                } else {
                    progress.insert { table ->
                        table[this.guid] = entry.guid
                        table[this.type] = entry.type
                        table[this.title] = entry.title
                        table[this.progress] = entry.progress
                        table[this.duration] = entry.duration
                        table[this.played] = entry.played
                        table[this.video] = entry.video ?: ""
                        table[this.collection] = entry.collection ?: ""
                        table[this.episode] = entry.episode ?: (-99..-1).random()
                        table[this.season] = entry.season ?: (-99..-1).random()
                    }
                }
            }

        }
    }

    private fun selectSerieRecordOnGuidAndCombinationValues(it: ProgressTable): ResultRow? {
        return transaction {
            progress
                .select { progress.guid eq it.guid }
                .andWhere { progress.collection eq (it.collection ?: "") }
                .andWhere { progress.type eq it.type }
                .andWhere { progress.episode eq (it.episode ?: -1) }
                .andWhere { progress.season eq (it.season ?: -1) }
                .singleOrNull()
        }
    }

    private fun serieToProgressTable(serie: ProgressSerie): List<ProgressTable> {
        return serie.seasons.flatMap { season -> season.episodes.map {
            ProgressTable(
                id = -1,
                guid = serie.guid,
                type = "serie",
                title = serie.title,
                collection = serie.collection,
                video = it.video,
                season = season.season,
                episode = it.episode,
                progress = it.progress,
                duration = it.duration,
                played = it.played
            )
        }}
    }


}