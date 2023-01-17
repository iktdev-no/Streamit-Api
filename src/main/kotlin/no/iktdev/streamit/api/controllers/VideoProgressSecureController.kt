package no.iktdev.streamit.api.controllers

import no.iktdev.streamit.api.classes.*
import no.iktdev.streamit.api.controllers.annotations.Authentication
import no.iktdev.streamit.api.controllers.annotations.AuthenticationModes
import no.iktdev.streamit.api.controllers.logic.VideoProgressLogic
import no.iktdev.streamit.api.services.database.ProgressService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping(path = ["/secure"])
class VideoProgressSecureController {

    @Authentication(AuthenticationModes.SOFT)
    @GetMapping("/progress/{guid}")
    fun allProgressOnGuid(@PathVariable guid: String): List<BaseProgress> {
        return VideoProgressLogic.Get().getProgressOnGuid(guid)
    }

    @Authentication(AuthenticationModes.SOFT)
    @GetMapping("/progress/{guid}/movie")
    fun allProgressOnGuidForMovies(@PathVariable guid: String): List<ProgressMovie> {
        return VideoProgressLogic.Get().getProgressOnGuidForMovies(guid)
    }

    @Authentication(AuthenticationModes.SOFT)
    @GetMapping("/progress/{guid}/serie")
    fun allProgressOnGuidForSerie(@PathVariable guid: String): List<ProgressSerie> {
        return VideoProgressLogic.Get().getProgressOnGuidForSeries(guid)
    }

    @Authentication(AuthenticationModes.SOFT)
    @GetMapping("/progress/{guid}/movie/{title}")
    fun getProgressForUserWithMovieTitle(@PathVariable guid: String, @PathVariable title: String): ProgressMovie? {
        return VideoProgressLogic.Get().getProgressOnGuidWithMovieTitle(guid, title)
    }

    @Authentication(AuthenticationModes.SOFT)
    @GetMapping("/progress/{guid}/serie/{title}")
    fun getProgressForUserWithSerieTitle(@PathVariable guid: String, @PathVariable title: String): ProgressSerie? {
        return VideoProgressLogic.Get().getProgressOnGuidWithSerieTitle(guid, title)
    }

    @Authentication(AuthenticationModes.SOFT)
    @GetMapping("/progress/{guid}/continue")
    fun getContinue(@PathVariable guid: String): List<BaseCatalog> {
        return VideoProgressLogic.Get().getContinueOnForGuid(guid)
    }

    /**
    * Post mapping below
    **/

    @Authentication(AuthenticationModes.STRICT)
    @PostMapping("/progress/movie")
    @ResponseStatus(HttpStatus.OK)
    fun uploadedProgressMovie(@RequestBody progress: ProgressMovie) : ResponseEntity<String> {
        return VideoProgressLogic.Post().updateOrInsertProgressForMovie(progress)
    }

    @Authentication(AuthenticationModes.STRICT)
    @PostMapping("/progress/serie")
    @ResponseStatus(HttpStatus.OK)
    fun uploadedProgressSerie(@RequestBody progress: ProgressSerie): ResponseEntity<String> {
        return VideoProgressLogic.Post().updateOrInsertProgressForSerie(progress)
    }
}