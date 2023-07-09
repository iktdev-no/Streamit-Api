package no.iktdev.streamit.api.controllers.logic

import no.iktdev.streamit.api.classes.Summary
import no.iktdev.streamit.api.database.queries.QSummary

class SummaryLogic {

    class Get {
        fun getSummaryById(id: Int): List<Summary> {
            return QSummary().selectOnId(id)
        }
    }
}