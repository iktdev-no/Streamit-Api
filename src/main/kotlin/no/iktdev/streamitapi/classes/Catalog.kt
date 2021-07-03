package no.iktdev.streamitapi.classes

import no.iktdev.streamitapi.database.catalog
import no.iktdev.streamitapi.database.genre
import no.iktdev.streamitapi.database.movie
import no.iktdev.streamitapi.database.serie
import org.jetbrains.exposed.sql.ResultRow

abstract class BaseCatalog
{
    abstract val id: Int
    abstract val title: String
    abstract val cover: String?
    abstract val type: String
    abstract val collection: String?
    abstract var genres: String?
    abstract var iid: Int
}

abstract class BaseEpisode
{
    abstract val episode: Int
    abstract val video: String
}


data class Catalog(override val id: Int, override val title: String, override val cover: String?, override val type : String, override val collection: String?, override var iid: Int,
                   override var genres: String?
)
    : BaseCatalog()
{
    companion object
    {
        fun fromRow(resultRow: ResultRow) = Catalog(
            id = resultRow[catalog.id].value,
            title = resultRow[catalog.title],
            cover = resultRow[catalog.cover],
            type = resultRow[catalog.type],
            collection = resultRow[catalog.collection],
            genres = resultRow[catalog.genres],
            iid = (resultRow.getOrNull(catalog.iid) ?: 0) //  [catalog.iid] ?: 0)
        )
    }
}

data class Movie(override val id: Int, // id will be catalog id
                 val video: String,
                 override val title: String,
                 override val cover: String?,
                 override val type: String,
                 override val collection: String?,
                 override var genres: String?,
                 override var iid: Int // iid will be movie id
)
    : BaseCatalog()
{
    companion object
    {
        fun fromRow(resultRow: ResultRow) = Movie(
            id = resultRow[catalog.id].value,
            video = resultRow[movie.video],
            title = resultRow[catalog.title],
            cover = resultRow[catalog.cover],
            type = resultRow[catalog.type],
            collection = resultRow[catalog.collection],
            genres = resultRow[catalog.genres],
            iid = resultRow[movie.id].value,
        )

        fun toCatalog(it: Movie) = Catalog(
            id = it.id,
            title = it.title,
            cover = it.cover,
            type = it.type,
            collection = it.collection,
            genres = it.genres,
            iid = it.iid
        )
    }
}

data class Episode(override val episode: Int, val title: String?, override val video: String)
    : BaseEpisode()
{
    companion object
    {
        fun fromFlat(item: SerieFlat) = Episode(
            episode = item.episode,
            title = item.episodeTitle,
            video = item.video
        )
    }
}

data class Season(val season: Int, val episodes: List<Episode>)
{
    companion object
    {
        fun fromFlat(item: SerieFlat) = Season(
            season = item.season,
            episodes = listOf(Episode.fromFlat(item))
        )
    }
}

data class Serie(var seasons: List<Season>,
                 override val id: Int,
                 override val title: String,
                 override val cover: String?,
                 override val type: String,
                 override val collection: String?,
                 override var genres: String?,
                 override var iid: Int): BaseCatalog()
{
                     companion object
                     {
                         /**
                          * NOTE! This will not populate season array!
                          */
                         fun fromFlat(item: SerieFlat) = Serie(
                             id = item.id,
                             title = item.title,
                             cover = item.cover,
                             type = item.type,
                             collection = item.collection,
                             genres = item.genres,
                             iid = item.iid,
                             seasons = listOf() // listOf(Season.fromFlat(item))
                         )

                         fun toCatalog(it: Serie) = Catalog(
                             id = it.id,
                             title = it.title,
                             cover = it.cover,
                             type = it.type,
                             genres = it.genres,
                             collection = it.collection,
                             iid = 0
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
    override var iid: Int,
    val season: Int,
    val episode: Int,
    val episodeTitle: String?,
    val video: String
): BaseCatalog()
{
    companion object
    {
        fun fromRow(resultRow: ResultRow) = SerieFlat(
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
            iid = 0
        )
    }
}