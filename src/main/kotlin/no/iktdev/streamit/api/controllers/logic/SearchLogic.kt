package no.iktdev.streamit.api.controllers.logic

import no.iktdev.streamit.api.classes.Catalog
import no.iktdev.streamit.api.database.queries.QSearch

class SearchLogic {

    fun movieSearch(keyword: String): List<Catalog> {
        return QSearch().movieOn(keyword)
    }

    fun serieSearch(keyword: String): List<Catalog> {
        return QSearch().serieOn(keyword)

    }

    fun catalogSearch(keyword: String): List<Catalog> {
        return QSearch().allOn(keyword)

    }
}