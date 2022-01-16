package no.iktdev.streamitapi.controllers

import no.iktdev.streamitapi.Configuration
import no.iktdev.streamitapi.classes.*
import no.iktdev.streamitapi.database.DataSource
import no.iktdev.streamitapi.database.progress
import no.iktdev.streamitapi.helper.progressHelper
import no.iktdev.streamitapi.services.database.ProgressService
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
class ProgressController
{
    /**
     * Returns super class
     */
    @GetMapping("/progress/{guid}")
    fun getProgressForUser(@PathVariable guid: String): List<BaseProgress>
    {
        val _progress: MutableList<ProgressTable> = mutableListOf()
        transaction(DataSource().getConnection()) {
            progress
                .select { progress.guid eq guid }
                .mapNotNull {
                    _progress.add(ProgressTable.fromRow(it))
                }
        }
        //System.out.println(_progress)
        val mixedProgress: List<BaseProgress> = progressHelper.map().fromMixedProgressTable(_progress)
        return mixedProgress
    }

    @GetMapping("/progress/{guid}/movie/{title}")
    fun getProgressForUserWithMovieTitle(@PathVariable guid: String, @PathVariable title: String): ProgressMovie?
    {
        var _progress: ProgressMovie? = null
        transaction(DataSource().getConnection()) {
            val result = progress
                .select { progress.guid eq guid }
                .andWhere { progress.title eq title }
                .singleOrNull()
            if (result != null) {
                _progress = ProgressMovie.fromRow(result)
            }
        }
        return _progress
    }

    @GetMapping("/progress/{guid}/serie/{title}")
    fun getProgressForUserWithSerieTitle(@PathVariable guid: String, @PathVariable title: String): ProgressSerie?
    {
        var serieProgress: ProgressSerie? = null
        transaction(DataSource().getConnection()) {
            val _progress: MutableList<ProgressTable> = mutableListOf()
            val result = progress
                .select { progress.guid eq guid }
                .andWhere { progress.collection eq title }
                .mapNotNull {
                    _progress.add(ProgressTable.fromRow(it))
                }
            if (result.count() > 0)
            {
                serieProgress = progressHelper.map().mergeSerieTables(_progress)
            }

        }
        return serieProgress
    }


    @GetMapping("/progress/{guid}/movie")
    fun getMovieProgressForUser(@PathVariable guid: String): List<ProgressMovie>?
    {
        System.out.println(":|")
        val _progress: MutableList<ProgressMovie> = mutableListOf()
        transaction(DataSource().getConnection()) {
            progress
                .select { progress.guid eq guid }
                .andWhere { progress.type eq "movie" }
                .mapNotNull {
                    _progress.add(ProgressMovie.fromRow(it))
                }
        }
        return _progress
    }


    @GetMapping("/progress/{guid}/serie")
    fun getSerieProgressForUser(@PathVariable guid: String): List<ProgressSerie>?
    {
        val serieProgress: MutableList<ProgressSerie> = mutableListOf()
        transaction() {
            val _progress: MutableList<ProgressTable> = mutableListOf()
            val result = progress
                .select { progress.guid eq guid }
                .andWhere { progress.type eq "serie" }
                .mapNotNull {
                    _progress.add(ProgressTable.fromRow(it))
                }
            if (result.count() > 0)
            {
                serieProgress.addAll(progressHelper.map().fromSerieProgressTable(_progress))
            }

        }
        return serieProgress
    }



    /**
     * Is only valid for serie (at the moment)
     */
    @GetMapping("/progress/{guid}/continue")
    fun getContinue(@PathVariable guid: String):  List<BaseProgress>
    {
        val _progress: MutableList<BaseProgress> = mutableListOf()
        val table: MutableList<ProgressTable> = mutableListOf()
        transaction {
            val msx = progress.season.max().alias(progress.season.name)
            val mex = progress.episode.max().alias(progress.episode.name)

            val episodeTable = progress.slice(
                mex,
                progress.title
            ).selectAll().groupBy(progress.title).alias("episodeTable")

            val seasonTable = progress.slice(
                msx,
                progress.title
            ).selectAll().groupBy(progress.title).alias("seasonTable")

            progress
                .join(episodeTable, JoinType.INNER)
                {
                    progress.title eq episodeTable[progress.title] and(progress.episode eq  episodeTable[mex])
                }
                .join(seasonTable, JoinType.INNER)
                {
                    progress.title eq seasonTable[progress.title] and(progress.season eq seasonTable[msx])
                }
                .select { progress.played.isNotNull() }
                .andWhere { progress.guid eq guid }
                .orderBy(progress.played, SortOrder.DESC)
                .limit(Configuration.continueWatch)
                .mapNotNull {
                    table.add(ProgressTable.fromRow(it))
                }
        }
        if (table.size > 0) {
            _progress.addAll(progressHelper.map().fromMixedProgressTable(table))
        }

        return _progress
    }



    /*
    * Post mapping below
    * */


    @PostMapping("/progress/movie")
    @ResponseStatus(HttpStatus.OK)
    fun uploadedProgressMovie(@RequestBody progress: ProgressMovie) : Response
    {
        //System.out.println(progress)
        ProgressService().upsertProgressMovie(progress)
        return Response()
    }

    @PostMapping("/progress/serie")
    @ResponseStatus(HttpStatus.OK)
    fun uploadedProgressSerie(@RequestBody progress: ProgressSerie): Response
    {
        //System.out.println(progress)
        ProgressService.validate().serie(progress)
        ProgressService().upsertProgressSerie(progress)
        return Response()
    }



}