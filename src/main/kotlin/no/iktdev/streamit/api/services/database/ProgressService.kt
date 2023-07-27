package no.iktdev.streamit.api.services.database

import no.iktdev.streamit.api.classes.ProgressMovie
import no.iktdev.streamit.api.classes.ProgressSerie
import no.iktdev.streamit.api.classes.ProgressTable
import no.iktdev.streamit.api.database.timestampToLocalDateTime
import no.iktdev.streamit.api.database.toEpochSeconds
import no.iktdev.streamit.api.helper.progressHelper
import no.iktdev.streamit.library.db.tables.progress
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class ProgressService
{

    fun upsertProgressMovie(progressMovie: ProgressMovie)
    {
        transaction {
            val found = progress
                .select { progress.guid eq progressMovie.guid }
                .andWhere { progress.title eq progressMovie.title.trim() }
                .andWhere { progress.type eq progressMovie.type }
                .singleOrNull()
            run {
                if (found != null) {
                    var _progress = progressMovie.progress
                    var _duration = progressMovie.progress
                    var _played = progressMovie.played

                    if (found[progress.progress] > progressMovie.progress) {
                        println("Re-using DB data " + progressMovie.title)
                        println("Reason: Present progress is larger than passed..")
                        _progress = found[progress.progress]
                    }

                    if (found[progress.duration] > progressMovie.duration) {
                        if (progressMovie.duration == 0) {
                            println("Duration passed is 0! Please verify that the application is recording as intended!")
                        } else {
                            println("Duration passed is less than data present in database, please verify that the content is the same!")
                        }
                        _duration = found[progress.duration]
                    }


                    if ((found[progress.played]?.toEpochSeconds() ?: 0) > progressMovie.played) {
                        _played = (found[progress.played]?.toEpochSeconds() ?: 0).toInt()
                    }

                    progress.update({ progress.id eq found[progress.id] })
                    {
                        it[this.progress] = _progress
                        it[this.duration] = _duration
                        it[this.played] = timestampToLocalDateTime(_played)
                        it[this.video] = progressMovie.video ?: ""
                    }
                }
                else
                {
                    progress.insert {
                        it[this.guid] = progressMovie.guid
                        it[this.type] = progressMovie.type
                        it[this.title] = progressMovie.title.trim()
                        it[this.progress] = progressMovie.progress
                        it[this.duration] = progressMovie.duration
                        it[this.played] = timestampToLocalDateTime(progressMovie.played)
                        it[this.video] = progressMovie.video ?: ""
                    }
                }
            }
        }
    }

    fun upsertProgressSerie(progressSerie: ProgressSerie)
    {
        val list: List<ProgressTable> = progressHelper.flatten().list(progressSerie)
        transaction {
            list.forEach {
                val found = progress
                    .select { progress.guid eq it.guid }
                    .andWhere { progress.collection eq it.collection }
                    .andWhere { progress.type eq it.type }
                    .andWhere { progress.episode eq (it.episode ?: -1) }
                    .andWhere { progress.season eq (it.season ?: -1) }
                    .singleOrNull()
                if (found != null) {
                    val video = it.video ?: found[progress.video]
                    val title = it.title
                    var _progress = it.progress
                    var _duration = it.duration

                    if (found[progress.progress] > it.progress) {
                        println("Re-using DB data " + it.title + " - S" + it.season + " E" + it.episode)
                        println("Reason: Present progress is larger than passed..")
                        _progress = found[progress.progress]
                    }

                    if (found[progress.duration] > it.duration) {
                        if (it.duration == 0) {
                            println("Duration passed is 0! Please verify that the application is recording as intended!")
                        } else {
                            println("Duration passed is less than data present in database, please verify that the content is the same!")
                        }
                        _duration = found[progress.duration]
                    }

                    var _played = it.played
                    if ((found[progress.played]?.toEpochSeconds() ?: 0) > it.played) {
                        _played = (found[progress.played]?.toEpochSeconds() ?: 0).toInt()
                    }

                    progress.update({ progress.id eq found[progress.id] }) { table ->
                        table[this.video] = video
                        table[this.progress] = _progress
                        table[this.duration] = _duration
                        table[this.played] = timestampToLocalDateTime(_played)
                        table[this.title] = title
                    }
                }
                else {
                    if (it.collection.isNullOrEmpty()) {
                        throw Error("Collection needs to be defined for serie progress!")
                    }
                    if (it.episode == -1 || it.season == -1
                        || it.episode == null || it.season == null) {
                        throw Error("Please provide a valid season or episode number (greater than -1)")
                    }
                    progress.insert { table ->
                        table[this.guid] = it.guid
                        table[this.type] = it.type
                        table[this.title] = it.title
                        table[this.progress] = it.progress
                        table[this.duration] = it.duration
                        table[this.played] = timestampToLocalDateTime(it.played)
                        table[this.video] = it.video ?: ""
                        table[this.collection] = it.collection
                        table[this.episode] = it.episode
                        table[this.season] = it.season
                    }
                }

            }
        }

    }

    class validate
    {
        fun serie(progressSerie: ProgressSerie)
        {
            if (progressSerie.collection.isNullOrEmpty()) {
                throw Error("Validate error: Collection is null or empty")
            }
        }

        /*fun movie(progressMovie: ProgressMovie)
        {
            if (progressMovie?.title.isNullOrEmpty()) {
                throw Error("Validate error: Collection is null or empty")
            }
        }*/
    }


}