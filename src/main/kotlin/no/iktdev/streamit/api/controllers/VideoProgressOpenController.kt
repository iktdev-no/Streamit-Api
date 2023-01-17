package no.iktdev.streamit.api.controllers

import no.iktdev.streamit.api.classes.*
import no.iktdev.streamit.api.controllers.logic.VideoProgressLogic
import no.iktdev.streamit.api.services.database.ProgressService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping(path = ["/open"])
class VideoProgressOpenController {

    @GetMapping("/progress/{guid}")
    fun allProgressOnGuid(@PathVariable guid: String): List<BaseProgress> {
        return VideoProgressLogic.Get().getProgressOnGuid(guid)
    }

    @GetMapping("/progress/{guid}/movie")
    fun allProgressOnGuidForMovies(@PathVariable guid: String): List<ProgressMovie> {
        return VideoProgressLogic.Get().getProgressOnGuidForMovies(guid)
    }

    @GetMapping("/progress/{guid}/serie")
    fun allProgressOnGuidForSerie(@PathVariable guid: String): List<ProgressSerie> {
        return VideoProgressLogic.Get().getProgressOnGuidForSeries(guid)
    }


    @GetMapping("/progress/{guid}/movie/{title}")
    fun getProgressForUserWithMovieTitle(@PathVariable guid: String, @PathVariable title: String): ProgressMovie? {
        return VideoProgressLogic.Get().getProgressOnGuidWithMovieTitle(guid, title)
    }

    @GetMapping("/progress/{guid}/serie/{title}")
    fun getProgressForUserWithSerieTitle(@PathVariable guid: String, @PathVariable title: String): ProgressSerie? {
        return VideoProgressLogic.Get().getProgressOnGuidWithSerieTitle(guid, title)
    }

    @GetMapping("/progress/{guid}/continue")
    fun getContinue(@PathVariable guid: String): List<BaseCatalog> {
        return VideoProgressLogic.Get().getContinueOnForGuid(guid)
    }

    @GetMapping("/progress/{guid}/continue/serie")
    fun getContinueSerie(@PathVariable guid: String): List<Serie> {
        return VideoProgressLogic.Get().getContinueSerieOnForGuid(guid)
    }

    /**
    * Post mapping below
    **/


    @PostMapping("/progress/movie")
    @ResponseStatus(HttpStatus.OK)
    fun uploadedProgressMovie(@RequestBody progress: ProgressMovie) : ResponseEntity<String> {
        return VideoProgressLogic.Post().updateOrInsertProgressForMovie(progress)
    }

    @PostMapping("/progress/serie")
    @ResponseStatus(HttpStatus.OK)
    fun uploadedProgressSerie(@RequestBody progress: ProgressSerie): ResponseEntity<String> {
        return VideoProgressLogic.Post().updateOrInsertProgressForSerie(progress)
    }
}