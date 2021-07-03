package no.iktdev.streamitapi.services

import net.dzikoysk.exposed.upsert.upsert
import no.iktdev.streamitapi.classes.BaseProgress
import no.iktdev.streamitapi.classes.ProgressMovie
import no.iktdev.streamitapi.classes.ProgressSerie
import no.iktdev.streamitapi.classes.ProgressTable
import no.iktdev.streamitapi.database.progress
import no.iktdev.streamitapi.helper.progressHelper
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.stereotype.Service

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
                    progress.update({ progress.id eq found[progress.id] })
                    {
                        it[this.progress] = progressMovie.progress
                        it[this.duration] = progressMovie.duration
                        it[this.played] = progressMovie.played
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
                        it[this.played] = progressMovie.played
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
            list.forEach() {
                val found = progress
                    .select { progress.guid eq it.guid }
                    .andWhere { progress.collection eq (it.collection ?: "") }
                    .andWhere { progress.type eq it.type }
                    .andWhere { progress.episode eq (it.episode ?: -1) }
                    .andWhere { progress.season eq (it.season ?: -1) }
                    .singleOrNull()
                if (found != null) {
                    val video = it.video ?: found[progress.video]
                    val title = it.title ?: found[progress.title]
                    progress.update({ progress.id eq found[progress.id] }) { table ->
                        table[this.video] = video
                        table[this.progress] = it.progress
                        table[this.duration] = it.duration
                        table[this.played] = it.played
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
                        table[this.played] = it.played
                        table[this.video] = it.video ?: ""
                        table[this.collection] = it.collection
                        table[this.episode] = it.episode ?: -1
                        table[this.season] = it.season ?: -1
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