package no.iktdev.streamit.api.classes

import no.iktdev.streamit.library.db.tables.catalog
import no.iktdev.streamit.library.db.tables.movie
import org.jetbrains.exposed.sql.ResultRow

enum class ContentType {
    Movie,
    Serie,
    Unknown
}

fun ContentType.sqlName(): String {
    return this.name.lowercase()
}

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
    abstract val season: Int
    abstract val episode: Int
    abstract val video: String
}


open class Catalog(
    val id: Int,
    val title: String,
    val cover: String?,
    val type: ContentType,
    val collection: String,
    val summary: List<Summary> = emptyList(),
    var genres: String?,
    val recent: Boolean
) {
    companion object {
        fun fromRow(resultRow: ResultRow, recent: Boolean = false) = Catalog(
            id = resultRow[catalog.id].value,
            title = resultRow[catalog.title],
            cover = resultRow[catalog.cover],
            type = resultRow[catalog.type].let {
                if (it.equals("movie", true))
                    ContentType.Movie
                else if (it.equals(
                        "serie",
                        true
                    )
                ) ContentType.Serie else ContentType.Unknown
            },
            collection = resultRow[catalog.collection],
            genres = resultRow[catalog.genres],
            recent = recent
        )
    }
}

class Movie(
    id: Int, // id will be catalog id
    title: String,
    cover: String? = null,
    collection: String,
    genres: String? = "",
    recent: Boolean = false,
    val video: String,
    var progress: Int = 0,
    var duration: Int = 0,
    var played: Int = 0,
    var subs: List<Subtitle> = emptyList()
) : Catalog(
    id = id,
    title = title,
    cover = cover,
    type = ContentType.Movie,
    collection = collection,
    genres = genres,
    recent = recent
) {
    companion object {
        fun fromRow(resultRow: ResultRow, recent: Boolean = false) = Movie(
            id = resultRow[catalog.id].value,
            video = resultRow[movie.video],
            title = resultRow[catalog.title],
            cover = resultRow[catalog.cover],
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

data class Episode(
    override val season: Int,
    override val episode: Int,
    val title: String?,
    override val video: String,
    var progress: Int = 0,
    var duration: Int = 0,
    var played: Int = 0,
    var subs: List<Subtitle> = emptyList()
) : BaseEpisode()


data class Serie(
    var episodes: List<Episode> = emptyList(),
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

    fun after(currentSeason: Int, currentEpisode: Int): Episode? {
        return episodes.filter { s -> s.season >= currentSeason }.firstOrNull { e -> e.episode >= currentEpisode }
    }
}
