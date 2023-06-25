package no.iktdev.streamit.api.classes

import no.iktdev.streamit.api.database.progress
import org.jetbrains.exposed.sql.ResultRow

data class ProgressTable(
    val id: Int,
    val guid: String,
    val type: String,
    val title: String,
    val collection: String,
    val episode: Int?,
    val season: Int?,
    val video: String?, // Might not be recorded
    val progress: Int,
    val duration: Int,
    val played: Int
)
{
    companion object
    {
        fun fromRow(resultRow: ResultRow) = ProgressTable(
            id = resultRow[progress.id].value,
            guid = resultRow[progress.guid],
            type = resultRow[progress.type],
            title = resultRow[progress.title],
            collection = resultRow[progress.collection] ?: resultRow[progress.title] ,
            episode = resultRow[progress.episode],
            season = resultRow[progress.season],
            video = resultRow[progress.video],
            progress = resultRow[progress.progress],
            duration =resultRow[progress.duration],
            played = resultRow[progress.played] ?: 0,
        )
    }
}

abstract class BaseProgress
{
    abstract val guid: String
    abstract val type: String
    abstract val title: String
    abstract val collection: String
}

data class ProgressMovie(
    override val guid: String,
    override val title: String,
    override val type: String,
    override val collection: String,
    val progress: Int,
    val duration: Int,
    var played: Int,
    val video: String?

): BaseProgress()
{
    companion object
    {
        fun fromProgressTable(item: ProgressTable) = ProgressMovie(
                guid = item.guid,
                title = item.title,
                type = item.type,
                video = item.video,
                collection = item.collection,
                progress = item.progress,
                duration = item.duration,
                played = item.played
            )
        fun fromRow(resultRow: ResultRow) = ProgressMovie(
            guid = resultRow[progress.guid],
            title = resultRow[progress.title],
            type = resultRow[progress.type],
            video = resultRow[progress.video],
            progress = resultRow[progress.progress],
            duration = resultRow[progress.duration],
            played = resultRow[progress.played] ?: 0,
            collection = resultRow[progress.collection] ?: resultRow[progress.title],
        )
    }
}

data class ProgressSerie(
    override val guid: String,
    override val type: String,
    override val title: String,
    override val collection: String,
    var seasons: List<ProgressSeason>
): BaseProgress()
{
    companion object
    {
        fun fromProgressTable(item: ProgressTable) = ProgressSerie(
            guid = item.guid,
            title = item.title,
            type = item.type,
            collection = item.collection,
            seasons = listOf()
        )
    }
}

data class ProgressSeason(val season: Int, var episodes: List<ProgressEpisode>)


data class ProgressEpisode(
    val episode: Int,
    val progress: Int,
    val duration: Int,
    val played: Int,
    val video: String?
)
{
    companion object
    {
        fun fromFlat(item: ProgressTable) = item.episode?.let {
            ProgressEpisode(
                episode = it,
                video = item.video,
                progress = item.progress,
                duration = item.duration,
                played = item.played
            )
        }
    }
}