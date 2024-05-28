package no.iktdev.streamit.api.database.queries

import mu.KotlinLogging
import no.iktdev.streamit.api.Configuration
import no.iktdev.streamit.api.classes.*
import no.iktdev.streamit.api.database.timestampToLocalDateTime
import no.iktdev.streamit.library.db.tables.progress
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

val log = KotlinLogging.logger {}

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


    fun selectSerieForGuidAfter(guid: String, time: Int): List<ProgressTable> {
        return transaction {
            progress.select { progress.guid eq guid }
                .andWhere { progress.type eq "serie" }
                .andWhere { progress.played greater timestampToLocalDateTime(time) }
                .mapNotNull { ProgressTable.fromRow(it) }
        }
    }

    fun selectMovieForGuidAfter(guid: String, time: Int): List<ProgressTable> {
        return transaction {
            progress.select { progress.guid eq guid }
                .andWhere { progress.type eq "movie" }
                .andWhere { progress.played greater timestampToLocalDateTime(time) }
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
                .select { progress.played.isNotNull() and progress.played.greater(timestampToLocalDateTime(0)) }
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

            val episodeTable = progress.slice(mex, progress.title).selectAll().andWhere { progress.progress greater 0 }.groupBy(progress.title).alias("episodeTable")
            val seasonTable = progress.slice(msx, progress.title).selectAll().andWhere { progress.progress greater 0 }.groupBy(progress.title).alias("seasonTable")

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

    fun selectLastEpisodeForGuidAndTitle(guid: String, title: String): ProgressTable? {
        return transaction {
            val msx = progress.season.max().alias(progress.season.name)
            val mex = progress.episode.max().alias(progress.episode.name)

            val episodeTable = progress.slice(mex, progress.title).selectAll().andWhere { progress.progress greater 0 }.groupBy(progress.title).alias("episodeTable")
            val seasonTable = progress.slice(msx, progress.title).selectAll().andWhere { progress.progress greater 0 }.groupBy(progress.title).alias("seasonTable")

            val result = progress
                .join(episodeTable, JoinType.INNER) {
                    progress.title eq episodeTable[progress.title] and(progress.episode eq  episodeTable[mex])
                }
                .join(seasonTable, JoinType.INNER) {
                    progress.title eq seasonTable[progress.title] and(progress.season eq seasonTable[msx])
                }
                .select { progress.played.isNotNull() }
                .andWhere { progress.guid eq guid }
                .andWhere { progress.type eq "serie" }
                .andWhere { progress.title eq title }
                .orderBy(progress.played, SortOrder.DESC)
                .firstOrNull()
            result?.let { ProgressTable.fromRow(it) }
        }
    }


    fun upsertMovieOnGuid(guid: String, movie: Movie) {
        if (movie.duration == 0) {
            log.error("${movie.title}: Duration is 0")
        } else if (movie.video.isEmpty()) {
            log.error("${movie.title}: Video is null or empty")
        }
        if (movie.played <= 0)
            movie.played = (System.currentTimeMillis() / 1000L).toInt()

        transaction {
            val presentRecord = selectMovieRecordOnGuidAndTitle(guid, movie.title)
            if (presentRecord != null) {
                progress.update({ progress.id eq presentRecord[progress.id] })
                {
                    it[this.progress] = movie.progress
                    it[this.duration] = movie.duration
                    it[this.collection] = movie.collection
                    it[this.played] = timestampToLocalDateTime(movie.played)
                    if (movie.video.isNotBlank() == true) {
                        it[this.video] = movie.video
                    }
                }
            } else {
                progress.insert {
                    it[this.guid] = guid
                    it[this.type] = movie.type.sqlName()
                    it[this.title] = movie.title.trim()
                    it[this.progress] = movie.progress
                    it[this.duration] = movie.duration
                    it[this.collection] = movie.collection
                    it[this.played] = timestampToLocalDateTime(movie.played)
                    if (movie.video.isNotBlank()) {
                        it[this.video] = movie.video
                    }
                }
            }
        }
        QResumeOrNext(guid).setResume(movie)
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


    fun upsertSerieOnGuid(guid: String, serie: Serie) {
        /*if (serie.seasons.flatMap { it.episodes }.any {  it.duration <= 0 }) {
        }*/
        val mapped = serieToProgressTableWithGuid(guid, serie)
        mapped.forEach { entry ->
            if (entry.duration == 0 && entry.progress == 0) {
                log.error("Skipping: $entry")
            } else {
                val record = selectSerieRecordOnGuidAndCombinationValues(entry)
                if (entry.progress == 0 && (record?.get(progress.progress) ?: 0) != 0) {
                    log.error("Skipping: $entry as it looks like progress is lost")
                } else {
                    transaction {
                        if (record != null) {
                            progress.update({ progress.id eq record[progress.id] }) { table ->
                                table[this.video] = video
                                table[this.progress] = entry.progress
                                table[this.duration] = entry.duration
                                table[this.played] = timestampToLocalDateTime(entry.played)
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
                                table[this.played] = timestampToLocalDateTime(entry.played)
                                table[this.video] = entry.video ?: ""
                                table[this.collection] = entry.collection
                                table[this.episode] = entry.episode ?: (-99..-1).random()
                                table[this.season] = entry.season ?: (-99..-1).random()
                            }
                        }
                    }
                }
            }
        }
        QResumeOrNext(guid).setResume(serie)
    }


    private fun selectSerieRecordOnGuidAndCombinationValues(it: ProgressTable): ResultRow? {
        return transaction {
            progress
                .select { progress.guid eq it.guid }
                .andWhere { progress.collection eq it.collection }
                .andWhere { progress.type eq it.type }
                .andWhere { progress.episode eq (it.episode ?: -1) }
                .andWhere { progress.season eq (it.season ?: -1) }
                .singleOrNull()
        }
    }

    private fun serieToProgressTableWithGuid(guid: String, serie: Serie): List<ProgressTable> {
        return serie.episodes.map {
            ProgressTable(
                id = -1,
                guid = guid,
                type = "serie",
                title = serie.title,
                collection = serie.collection,
                video = it.video,
                season = it.season,
                episode = it.episode,
                progress = it.progress,
                duration = it.duration,
                played = it.played
            )
        }
    }

    @Deprecated("Now")
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