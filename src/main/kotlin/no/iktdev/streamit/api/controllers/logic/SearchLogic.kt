package no.iktdev.streamit.api.controllers.logic

import no.iktdev.streamit.api.classes.Catalog
import no.iktdev.streamit.api.database.catalog
import org.jetbrains.exposed.sql.andWhere
import org.jetbrains.exposed.sql.orWhere
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction

class SearchLogic {

    fun movieSearch(keyword: String): List<Catalog> {
        return transaction {
            catalog.select { catalog.title like "$keyword%" }
                .andWhere { catalog.type eq "movie" }
                .mapNotNull { Catalog.fromRow(it) }
        }
    }

    fun serieSearch(keyword: String): List<Catalog> {
        return transaction {
            catalog.select { catalog.title like "$keyword%" }
                .orWhere { catalog.collection like "%$keyword%" }
                .andWhere { catalog.type eq "serie" }
                .mapNotNull { Catalog.fromRow(it) }
        }
    }

    fun catalogSearch(keyword: String): List<Catalog> {
        return transaction {
            catalog.select { catalog.title like "$keyword%" }
                .mapNotNull { Catalog.fromRow(it) }
        }
    }
}