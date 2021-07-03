package no.iktdev.streamitapi.controllers

import no.iktdev.streamitapi.classes.*
import no.iktdev.streamitapi.database.DataSource
import no.iktdev.streamitapi.database.progress
import no.iktdev.streamitapi.helper.progressHelper
import no.iktdev.streamitapi.services.ProgressService
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
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
        System.out.println(_progress)
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






    /*
    * Post mapping below
    * */


    @PostMapping("/progress/movie")
    fun uploadedProgressMovie(@RequestBody progress: ProgressMovie)
    {
        System.out.println(progress)
        ProgressService().upsertProgressMovie(progress)
    }

    @PostMapping("/progress/serie")
    fun uploadedProgressSerie(@RequestBody progress: ProgressSerie)
    {
        System.out.println(progress)
        ProgressService.validate().serie(progress)
        ProgressService().upsertProgressSerie(progress)
    }



}