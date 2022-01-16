package no.iktdev.streamitapi.services.operation

import no.iktdev.streamitapi.Configuration
import no.iktdev.streamitapi.classes.Response
import no.iktdev.streamitapi.getContext
import no.iktdev.streamitapi.services.database.CatalogService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.io.File

@Service
class CatalogItemRemovalService {

    private fun getService(): CatalogService? {
        return getContext()?.getBean(CatalogService::class.java)
    }

    private fun getSubtitleItemPath(title: String): File? {
        return if (Configuration.content == null || Configuration.content?.exists() == false) null
        else File(Configuration.content, "subtitle/$title")
    }


    private fun getMoveItemPath(title: String): File? {
        return if (Configuration.content == null || Configuration.content?.exists() == false) null
            else File(Configuration.content, "movie/$title")
    }


    fun removeMovie(id: Int): Response {
        val result = getService()?.fetch?.getMovieFile(id)
        if (result.isNullOrEmpty()) {
            return Response(false, "No id matched")
        }
        return deleteMovie(result)
    }

    fun removeMovie(title: String): Response {
        val result = getService()?.fetch?.getMovieFile(title)
        if (result.isNullOrEmpty()) {
            return Response(false, "No title matched")
        }
        return deleteMovie(result)
    }

    private fun deleteMovie(fileName: String): Response {
        val file = getMoveItemPath(fileName)
        if (file == null || !file.exists()) {
            LoggerFactory.getLogger(javaClass.simpleName).info("File not found, no action performed..")
            return Response(false, "File not found, no action performed...")
        }

        if (file.delete()) { LoggerFactory.getLogger(javaClass.simpleName).info("Failed to delete file, none changes made.."); return Response(false, "Failed to delete file..") }

        val subtitleFile = getSubtitleItemPath(file.nameWithoutExtension)
        if (subtitleFile != null && subtitleFile.exists()) {
            subtitleFile.delete()
        }

        val databaseResult = getService()?.removal?.removeMovie(fileName)
        val success = databaseResult != null && databaseResult.countMovie == 1
        return Response(success, if (success) "Deleted movie $fileName" else "Failed to remove data in database")

    }

    private fun getSerieItemPath(collection: String): File? {
        return if (Configuration.content == null || Configuration.content?.exists() == false) null
            else File(Configuration.content, "serie/$collection")
    }

    fun removeSerie(id: Int): Response {
        val result = getService()?.fetch?.getSerieCollection(id)
        if (result.isNullOrEmpty()) {
            return Response(false, "Could not find collection")
        }
        return removeSerie(result)
    }

    fun removeSerie(collection: String): Response {
        val file = getSerieItemPath(collection)

        if (file == null || !file.exists()) {
            LoggerFactory.getLogger(javaClass.simpleName).info("File not found, no action performed..")
            return Response(false, "Could not find file/directory")
        }

        if (file.delete()) { LoggerFactory.getLogger(javaClass.simpleName).info("Failed to delete file, none changes made.."); return Response(false, "Failed to delete file/directory") }

        val subtitleFile = getSubtitleItemPath(collection)
        if (subtitleFile != null && subtitleFile.exists()) {
            subtitleFile.delete()
        }

        val databaseResult = getService()?.removal?.removeSerie(collection)
        val success = databaseResult != null && databaseResult.countEpisode > 0
        return Response(success, if (success) "Deleted serie $collection" else "Failed to remove data in database")
    }




}