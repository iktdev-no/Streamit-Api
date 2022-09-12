package no.iktdev.streamit.api.controllers

import no.iktdev.streamit.api.classes.Subtitle
import no.iktdev.streamit.api.controllers.annotations.Authentication
import no.iktdev.streamit.api.controllers.annotations.AuthenticationModes
import no.iktdev.streamit.api.controllers.logic.SubtitleLogic
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

open class SubtitleController {

    fun movieSubtitle(title: String, format: String? = null) = SubtitleLogic().videoSubtitle(title, format)
    fun serieSubtitle(collection: String, format: String? = null) = SubtitleLogic().serieSubtitle(collection, format)

    @RestController
    @RequestMapping(path = ["/open/subtitle"])
    class Open: SubtitleController() {

        @GetMapping("/{name}")
        fun anySubtitle(@PathVariable name: String): List<Subtitle> {
            return SubtitleLogic().videoSubtitle(name, null)
        }

        @GetMapping(path = [
            "/movie/{title}",
            "/movie/{title}/{format}"
        ])
        fun movieSubtitleWithFormat(@PathVariable title: String, @PathVariable format: String? = null) = super.movieSubtitle(title, format)

        @GetMapping(path = [
            "/serie/{collection}",
            "/serie/{collection}/{format}"
        ])
        fun serieSubtitleWithFormat(@PathVariable collection: String, @PathVariable format: String? = null) = super.serieSubtitle(collection, format)

    }

    @RestController
    @RequestMapping(path = ["/secure/subtitle"])
    class Secure: SubtitleController() {

        @Authentication(AuthenticationModes.SOFT)
        @GetMapping("/{name}")
        fun anySubtitle(@PathVariable name: String): List<Subtitle> {
            return SubtitleLogic().videoSubtitle(name, null)
        }

        @Authentication(AuthenticationModes.SOFT)
        @GetMapping(path = [
            "/movie/{title}",
            "/movie/{title}/{format}"
        ])
        fun movieSubtitleWithFormat(@PathVariable title: String, @PathVariable format: String? = null) = super.movieSubtitle(title, format)

        @Authentication(AuthenticationModes.SOFT)
        @GetMapping(path = [
            "/serie/{collection}",
            "/serie/{collection}/{format}"
        ])
        fun serieSubtitleWithFormat(@PathVariable collection: String, @PathVariable format: String? = null) = super.serieSubtitle(collection, format)

    }

}