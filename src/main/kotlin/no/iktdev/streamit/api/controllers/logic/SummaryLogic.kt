package no.iktdev.streamit.api.controllers.logic

import no.iktdev.streamit.api.classes.Summary
import no.iktdev.streamit.api.database.queries.QSummary
import no.iktdev.streamit.api.database.summary
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction

class SummaryLogic {

    class Get {
        fun getSummaryById(id: Int): List<Summary> {
            return QSummary().selectOnId(id)
        }
    }
}