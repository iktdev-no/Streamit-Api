package no.iktdev.streamitapi.controllers

import no.iktdev.streamitapi.classes.Subtitle
import no.iktdev.streamitapi.database.subtitle
import org.jetbrains.exposed.sql.Query
import org.jetbrains.exposed.sql.andWhere
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController

@RestController
class SubtitleController
{

    fun sharedMovieSubtitle(title: String, format: String?): List<Subtitle>
    {
        val subtitles: MutableList<Subtitle> = mutableListOf()
        transaction { // transaction(DataSource().getConnection()) {
            val query: Query = subtitle
                .select { subtitle.title eq title }
            if (format != null)
                query.andWhere { subtitle.format eq format }

            query.mapNotNull {
                subtitles.add(Subtitle.fromRow(it))
            }
        }
        return subtitles
    }

    fun sharedSerieSubtitle(collection: String, format: String?): List<Subtitle>
    {
        val subtitles: MutableList<Subtitle> = mutableListOf()
        transaction { // transaction(DataSource().getConnection()) {
            val query: Query = subtitle
                .select { subtitle.collection eq collection }
            if (format != null)
                query.andWhere { subtitle.format eq format }

            query.mapNotNull {
                subtitles.add(Subtitle.fromRow(it))
            }
        }
        return subtitles
    }

    fun sharedAnySubtitle(baseName: String, format: String?): List<Subtitle>
    {
        val subtitles: MutableList<Subtitle> = mutableListOf()
        transaction { // transaction(DataSource().getConnection()) {
            val query: Query = subtitle
                .select { subtitle.title eq baseName }
            if (format != null)
                query.andWhere { subtitle.format eq format }

            query.mapNotNull {
                subtitles.add(Subtitle.fromRow(it))
            }
        }
        return subtitles
    }


    @GetMapping("/subtitle/movie/{title}/{format}")
    fun movieSubtitleWithFormat(@PathVariable title: String, @PathVariable format: String? = null): List<Subtitle>
    {
        return sharedMovieSubtitle(title, format)
    }

    @GetMapping("/subtitle/movie/{title}")
    fun movieSubtitle(@PathVariable title: String): List<Subtitle>
    {
        return sharedMovieSubtitle(title, null)
    }

    @GetMapping("/subtitle/serie/{collection}/{format}")
    fun serieSubtitleWithFormat(@PathVariable collection: String, @PathVariable format: String? = null): List<Subtitle>
    {
        return sharedSerieSubtitle(collection, format)
    }

    @GetMapping("/subtitle/serie/{collection}")
    fun serieSubtitle(@PathVariable collection: String): List<Subtitle>
    {
        return sharedSerieSubtitle(collection, null)
    }

    @GetMapping("/subtitle/{name}")
    fun anySubtitle(@PathVariable name: String): List<Subtitle>
    {
        return sharedAnySubtitle(name, null)
    }



}