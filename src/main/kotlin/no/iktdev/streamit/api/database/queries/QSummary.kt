package no.iktdev.streamit.api.database.queries

import no.iktdev.streamit.api.classes.Summary
import no.iktdev.streamit.library.db.tables.summary
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction

class QSummary {

    fun selectOnId(id: Int): List<Summary> {
        return transaction {
            summary.select { summary.cid eq id }
                .mapNotNull { Summary.fromRow(it) }
        }
    }
}