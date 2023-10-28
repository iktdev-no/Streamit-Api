package no.iktdev.streamit.api.classes

import no.iktdev.streamit.library.db.tables.catalog
import no.iktdev.streamit.library.db.tables.movie
import org.jetbrains.exposed.sql.ResultRow

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
    var subs: List<Subtitle> = emptyList()
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

data class Episode(override val episode: Int, val title: String?, override val video: String, var progress: Int = 0, var duration: Int = 0, var played: Int = 0, var subs: List<Subtitle> = emptyList()) : BaseEpisode()

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


data class Season<E: BaseEpisode>(val season: Int, val episodes: MutableList<E>)

data class Serie(
    var seasons: List<Season<Episode>> = emptyList(),
    override val id: Int,
    override val title: String,
    override val cover: String? = null,
    override val type: String,
    override val collection: String,
    override var genres: String?,
    override var recent: Boolean = false
) : BaseCatalog() {
    companion object {
        fun basedOn(row: ResultRow) = Serie(
            id = row[catalog.id].value,
            title = row[catalog.title],
            cover = row[catalog.cover],
            type = row[catalog.type],
            collection = row[catalog.collection],
            genres = row[catalog.genres],
        )
    }

    fun after(currentSeason: Int, currentEpisode: Int): Pair<Int, Episode>? {
        val viableSeasons = seasons.filter { sit -> sit.season >= currentSeason }
        val viablePair = viableSeasons.firstNotNullOfOrNull { sit ->
            val episodes = if (sit.season == currentSeason) {
                sit.episodes.filter { it.episode > currentEpisode }
            } else sit.episodes
            if (episodes.isNotEmpty())
                sit.season to episodes.first()
            else null
        }
        return viablePair?.let { it.first to it.second }
    }
}
