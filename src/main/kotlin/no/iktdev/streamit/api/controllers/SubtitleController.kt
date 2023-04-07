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

    @GetMapping(value = [
        "/movie/{title}",
        "/movie/{title}/{format}"
    ])
    open fun movieSubtitle(@PathVariable title: String, @PathVariable format: String? = null): List<Subtitle> {
        return SubtitleLogic().videoSubtitle(title, format)
    }

    @GetMapping(value = [
        "/serie/{collection}",
        "/serie/{collection}/{format}"
    ])
    open fun serieSubtitle(@PathVariable collection: String, @PathVariable format: String? = null): List<Subtitle> {
        return SubtitleLogic().serieSubtitle(collection, format)
    }

    @GetMapping("/{name}")
    open fun anySubtitle(@PathVariable name: String): List<Subtitle> {
        return SubtitleLogic().videoSubtitle(name, null)
    }

    @RestController
    @RequestMapping(path = ["/open/subtitle"])
    class OpenSubtitle: SubtitleController() {

    }

    @RestController
    @RequestMapping(path = ["/secure/subtitle"])
    class RestrictedSubtitle: SubtitleController() {

        @Authentication(AuthenticationModes.SOFT)

        @GetMapping("/{name}")
        override fun anySubtitle(@PathVariable name: String): List<Subtitle> {
            return anySubtitle(name)
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