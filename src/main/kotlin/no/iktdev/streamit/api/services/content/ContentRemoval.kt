package no.iktdev.streamit.api.services.content

import no.iktdev.streamit.api.Configuration
import no.iktdev.streamit.api.classes.Episode
import no.iktdev.streamit.api.classes.Movie
import no.iktdev.streamit.api.classes.Serie
import no.iktdev.streamit.api.classes.Subtitle
import no.iktdev.streamit.api.database.queries.QCatalog
import no.iktdev.streamit.api.database.queries.QMovie
import no.iktdev.streamit.api.database.queries.QSerie
import no.iktdev.streamit.api.database.queries.QSubtitle
import no.iktdev.streamit.api.getContext
import org.springframework.stereotype.Service
import java.io.File

@Service
class ContentRemoval {
    companion object {
        fun getService(): ContentRemoval? {
            return getContext()?.getBean(ContentRemoval::class.java)
        }
    }


    private fun getSubtitles(collection: String, video: String): List<Pair<Any, File>> {
        val files: MutableList<Pair<Any, File>> = mutableListOf()
        QSubtitle().selectSubtitlBasedOnTitleOrVideo(video).forEach {
            val file = File("${Configuration.content}/${collection}/sub/${it.language}/${it.subtitle}")
            if (file.exists())
                files.add(Pair(it, file))
        }
        return files
    }

    fun removeMovie(movie: Movie) {
        val files: MutableList<Pair<Any, File>> = mutableListOf()
        files += getSubtitles(movie.collection, movie.video)
        val movieFile = File("${Configuration.content}/${movie.collection}/${movie.video}")
        if (!movieFile.exists())
            return
        files.add(Pair(movie, movieFile))

        files.forEach {
            val dataObject = it.first
            if (dataObject is Subtitle) {
                val id = dataObject.id
                val success = QSubtitle().deleteSubtitleOnId(id)
                if (success)
                    it.second.delete()
            } else if (dataObject is Movie) {
                val success = QMovie().deleteMovieItemOn(dataObject.video)
                if (success) {
                    QCatalog().deleteCatalogItemOn(dataObject.id)
                }
            }
        }
    }


    fun removeSerie(serie: Serie) {
        val files: MutableList<Pair<Any, File>> = mutableListOf()
        serie.seasons.flatMap { it.episodes }.forEach {
            val file = File("${Configuration.content}/${serie.collection}/${it.video}")
            if (file.exists()) {
                val subs = getSubtitles(serie.collection, it.video)
                files.addAll(subs)
                files.add(Pair(it, file))
            }
        }

        files.forEach {
            val dataObject = it.first
            if (dataObject is Subtitle) {
                val id = dataObject.id
                val success = QSubtitle().deleteSubtitleOnId(id)
                if (success)
                    it.second.delete()
            } else if (dataObject is Episode) {
                val success = QSerie().deleteEpisodeOnVideo(dataObject.video)
                if (success) {
                    it.second.delete()
                }
            }
        }
        QCatalog().deleteCatalogItemOn(serie.id)
    }


}