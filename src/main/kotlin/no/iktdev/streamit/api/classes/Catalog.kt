package no.iktdev.streamit.api.classes

import no.iktdev.streamit.api.database.catalog
import no.iktdev.streamit.api.database.genre
import no.iktdev.streamit.api.database.movie
import no.iktdev.streamit.api.database.serie
import org.jetbrains.exposed.sql.ResultRow
import java.util.stream.Collectors

abstract class BaseCatalog {
    abstract val id: Int
    abstract val title: String
    abstract val cover: String?
    abstract val type: String
    abstract val collection: String
    abstract var genres: String?
    abstract val recent: Boolean // If true on serie, shoud display new episodes, if movie, should display just new
}

abstract class BaseEpisode {
    abstract val episode: Int
    abstract val video: String
}


data class Catalog(
    override val id: Int,
    override val title: String,
    override val cover: String?,
    override val type: String,
    override val collection: String,
    override var genres: String?,
    override val recent: Boolean
) : BaseCatalog() {
    companion object {
        fun fromRow(resultRow: ResultRow, recent: Boolean = false) = Catalog(
            id = resultRow[catalog.id].value,
            title = resultRow[catalog.title],
            cover = resultRow[catalog.cover],
            type = resultRow[catalog.type],
            collection = resultRow[catalog.collection],
            genres = resultRow[catalog.genres],
            recent = recent
        )
    }
}

data class Movie(
    override val id: Int, // id will be catalog id
    val video: String,
    override val title: String,
    override val cover: String? = null,
    override val type: String,
    override val collection: String,
    override var genres: String? = "",
    override val recent: Boolean = false,
    var progress: Int = 0,
    var duration: Int = 0,
    var played: Int = 0,
) : BaseCatalog() {
    companion object {
        fun fromRow(resultRow: ResultRow, recent: Boolean = false) = Movie(
            id = resultRow[catalog.id].value,
            video = resultRow[movie.video],
            title = resultRow[catalog.title],
            cover = resultRow[catalog.cover],
            type = resultRow[catalog.type],
            collection = resultRow[catalog.collection],
            genres = resultRow[catalog.genres],
            recent = recent
        )

        fun toCatalog(it: Movie) = Catalog(
            id = it.id,
            title = it.title,
            cover = it.cover,
            type = it.type,
            collection = it.collection,
            genres = it.genres,
            recent = it.recent
        )
    }
}

data class Episode(override val episode: Int, val title: String?, override val video: String, var progress: Int = 0, var duration: Int = 0, var played: Int = 0) : BaseEpisode() {
    companion object {
        fun fromFlat(item: SerieFlat) = Episode(
            episode = item.episode,
            title = item.episodeTitle,
            video = item.video
        )
    }
}

data class EpisodeWithProgress(
    override val episode: Int,
    val progress: Int,
    val duration: Int,
    val played: Int,
    override val video: String
): BaseEpisode()
{
    companion object
    {
        fun fromFlat(item: ProgressTable) = item.episode?.let {
            EpisodeWithProgress(
                episode = it,
                video = item.video ?: "",
                progress = item.progress,
                duration = item.duration,
                played = item.played
            )
        }
    }
}


data class Season<E: BaseEpisode>(val season: Int, val episodes: MutableList<E>) {
    companion object {
        fun fromFlat(item: SerieFlat) = Season(
            season = item.season,
            episodes = mutableListOf(Episode.fromFlat(item))
        )
    }
}

data class Serie(
    var seasons: List<Season<Episode>>,
    override val id: Int,
    override val title: String,
    override val cover: String? = null,
    override val type: String,
    override val collection: String,
    override var genres: String?, override val recent: Boolean
) : BaseCatalog() {
    companion object {
        fun mapFromFlat(rows: List<SerieFlat>): Serie {
            val serie = toBaseFromFlat(rows.first())
            val seasons: List<Season<Episode>> = rows.stream().collect(Collectors.groupingBy { it.season }).map {
                Season(it.key, it.value.map { eit -> Episode.fromFlat(eit) }.toMutableList())
            }
            serie.seasons = seasons
            return serie
        }

        private fun toBaseFromFlat(item: SerieFlat) = Serie(
            id = item.id,
            title = item.title,
            cover = item.cover,
            type = item.type,
            collection = item.collection,
            genres = item.genres,
            recent = false,
            seasons = listOf() // listOf(Season.fromFlat(item))
        )

        /**
         * NOTE! This will not populate season array!
         */
        fun fromFlat(item: SerieFlat, recent: Boolean = false) = Serie(
            id = item.id,
            title = item.title,
            cover = item.cover,
            type = item.type,
            collection = item.collection,
            genres = item.genres,
            recent = recent,
            seasons = listOf() // listOf(Season.fromFlat(item))
        )


        fun toCatalog(it: Serie) = Catalog(
            id = it.id,
            title = it.title,
            cover = it.cover,
            type = it.type,
            genres = it.genres,
            collection = it.collection,
            recent = it.recent
        )
    }

}

data class SerieFlat(
    override val id: Int,
    override val title: String,
    override val cover: String?,
    override val type: String,
    override val collection: String,
    override var genres: String?,
    val season: Int,
    val episode: Int,
    val episodeTitle: String?,
    val video: String,
    override val recent: Boolean
) : BaseCatalog() {
    companion object {
        fun fromRow(resultRow: ResultRow, recent: Boolean = false) = SerieFlat(
            id = resultRow[catalog.id].value,
            title = resultRow[catalog.title],
            cover = resultRow[catalog.cover],
            type = resultRow[catalog.type],
            collection = resultRow[catalog.collection],
            genres = resultRow[catalog.genres],
            season = resultRow[serie.season],
            episode = resultRow[serie.episode],
            episodeTitle = resultRow[serie.title],
            video = resultRow[serie.video],
            recent = recent
        )
    }
}