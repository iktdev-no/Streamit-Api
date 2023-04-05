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

    @GetMapping(path = [
        "/movie/{title}",
        "/movie/{title}/{format}"
    ])
    open fun movieSubtitle(title: String, format: String? = null) = SubtitleLogic().videoSubtitle(title, format)

    @GetMapping(path = [
        "/serie/{collection}",
        "/serie/{collection}/{format}"
    ])
    open fun serieSubtitle(collection: String, format: String? = null) = SubtitleLogic().serieSubtitle(collection, format)

    @RestController
    @RequestMapping(path = ["/open/subtitle"])
    class Open: SubtitleController() {

        @GetMapping("/{name}")
        fun anySubtitle(@PathVariable name: String): List<Subtitle> {
            return SubtitleLogic().videoSubtitle(name, null)
        }

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
        override fun movieSubtitle(title: String, format: String?): List<Subtitle> {
            return super.movieSubtitle(title, format)
        }

        @Authentication(AuthenticationModes.SOFT)
        override fun serieSubtitle(collection: String, format: String?): List<Subtitle> {
            return super.serieSubtitle(collection, format)
        }

    }

}