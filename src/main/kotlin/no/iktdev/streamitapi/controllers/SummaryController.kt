package no.iktdev.streamitapi.controllers

import no.iktdev.streamitapi.classes.Summary
import no.iktdev.streamitapi.database.summary
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController

@RestController
class SummaryController
{

    /**
     * id = Catalog Id
     */
    @GetMapping("/summary/{id}")
    fun summaryById(@PathVariable id: Int): List<Summary>
    {
        val _summary: MutableList<Summary> = mutableListOf()
        transaction {
            summary
                .select { summary.cid eq id }
                .mapNotNull {
                    _summary.add(Summary.fromRow(it))
                }
        }
        return _summary
    }

}