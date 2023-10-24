package no.iktdev.streamit.api.controllers.logic

import no.iktdev.streamit.api.classes.Catalog
import no.iktdev.streamit.api.classes.GenreCatalog
import no.iktdev.streamit.api.database.queries.QCatalog
import no.iktdev.streamit.api.database.queries.QGenre

class GenredCatalogLogic {

    fun getGenreToCatalog(): List<GenreCatalog> {
        val genres = QGenre().selectAll()
        val idToMap = genres.associate { it.id to GenreCatalog(it, mutableListOf<Catalog>()) }
        QCatalog().selectCatalogWhereGenreIsNotNull().forEach { catalog ->
            catalog.genres?.split(",")?.mapNotNull { gid -> gid.toIntOrNull() }?.forEach { genreId ->
                idToMap[genreId]?.catalog?.add(catalog)
            }
        }

        return idToMap.values.filter { it.catalog.size > 3 }
    }
}