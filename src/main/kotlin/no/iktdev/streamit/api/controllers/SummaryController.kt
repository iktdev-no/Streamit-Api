package no.iktdev.streamit.api.controllers

import no.iktdev.streamit.api.classes.Summary
import no.iktdev.streamit.api.controllers.annotations.Authentication
import no.iktdev.streamit.api.controllers.annotations.AuthenticationModes
import no.iktdev.streamit.api.controllers.logic.SummaryLogic
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

open class SummaryController {

    @GetMapping("/summary/{id}")
    open fun getSummaryById(@PathVariable id: Int): List<Summary> {
        return if (id > -1) SummaryLogic.Get().getSummaryById(id) else emptyList()
    }

    @RestController
    @RequestMapping(path = ["/open"])
    class OpenSummary: SummaryController()

    @RestController
    @RequestMapping(path = ["/secure"])
    class RestrictedSummary: SummaryController() {

        @Authentication(AuthenticationModes.SOFT)
        override fun getSummaryById(@PathVariable id: Int): List<Summary> {
            return super.getSummaryById(id)
        }
    }
}