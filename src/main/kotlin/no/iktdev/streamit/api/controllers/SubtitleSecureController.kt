package no.iktdev.streamit.api.controllers

import no.iktdev.streamit.api.classes.Subtitle
import no.iktdev.streamit.api.controllers.annotations.Authentication
import no.iktdev.streamit.api.controllers.annotations.AuthenticationModes
import no.iktdev.streamit.api.controllers.logic.SubtitleLogic
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(path = ["/secure"])
class SubtitleSecureController {

    @Authentication(AuthenticationModes.SOFT)
    @GetMapping("/subtitle/movie/{title}/{format}")
    fun movieSubtitleWithFormat(@PathVariable title: String, @PathVariable format: String? = null): List<Subtitle> {
        return SubtitleLogic().videoSubtitle(title, format)
    }

    @Authentication(AuthenticationModes.SOFT)
    @GetMapping("/subtitle/movie/{title}")
    fun movieSubtitle(@PathVariable title: String): List<Subtitle> {
        return SubtitleLogic().videoSubtitle(title, null)
    }

    @Authentication(AuthenticationModes.SOFT)
    @GetMapping("/subtitle/serie/{collection}/{format}")
    fun serieSubtitleWithFormat(@PathVariable collection: String, @PathVariable format: String? = null): List<Subtitle> {
        return SubtitleLogic().serieSubtitle(collection, format)
    }

    @Authentication(AuthenticationModes.SOFT)
    @GetMapping("/subtitle/serie/{collection}")
    fun serieSubtitle(@PathVariable collection: String): List<Subtitle> {
        return SubtitleLogic().serieSubtitle(collection, null)
    }

    @Authentication(AuthenticationModes.SOFT)
    @GetMapping("/subtitle/{name}")
    fun anySubtitle(@PathVariable name: String): List<Subtitle> {
        return SubtitleLogic().videoSubtitle(name, null)
    }

}