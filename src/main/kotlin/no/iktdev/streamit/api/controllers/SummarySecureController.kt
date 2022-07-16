package no.iktdev.streamit.api.controllers

import no.iktdev.streamit.api.classes.Summary
import no.iktdev.streamit.api.controllers.logic.SummaryLogic
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(path = ["/secure"])
class SummarySecureController {


    @GetMapping("/summary/{id}")
    fun getSummaryById(@PathVariable id: Int): List<Summary> {
        return if (id > -1) SummaryLogic.Get().getSummaryById(id) else emptyList()
    }
}