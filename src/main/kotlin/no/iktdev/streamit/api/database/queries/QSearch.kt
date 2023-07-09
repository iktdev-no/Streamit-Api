package no.iktdev.streamit.api.database.queries

import no.iktdev.streamit.api.classes.Catalog
import no.iktdev.streamit.library.db.tables.catalog
import org.jetbrains.exposed.sql.andWhere
import org.jetbrains.exposed.sql.orWhere
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction

class QSearch {

    fun movieOn(keyword: String): List<Catalog> {
        return transaction {
            catalog.select { catalog.title like "$keyword%" }
                .andWhere { catalog.type eq "movie" }
                .mapNotNull { Catalog.fromRow(it) }
        }
    }

    fun serieOn(keyword: String): List<Catalog> {
        return transaction {
            catalog.select { catalog.title like "$keyword%" }
                .orWhere { catalog.collection like "%$keyword%" }
                .andWhere { catalog.type eq "serie" }
                .mapNotNull { Catalog.fromRow(it) }
        }
    }

    fun allOn(keyword: String): List<Catalog> {
        return transaction {
            catalog.select { catalog.title like "$keyword%" }
                .mapNotNull { Catalog.fromRow(it) }
        }
    }

}