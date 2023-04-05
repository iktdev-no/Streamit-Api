package no.iktdev.streamit.api.controllers

import no.iktdev.streamit.api.classes.*
import no.iktdev.streamit.api.controllers.annotations.Authentication
import no.iktdev.streamit.api.controllers.annotations.AuthenticationModes
import no.iktdev.streamit.api.controllers.logic.VideoProgressLogic
import no.iktdev.streamit.api.services.database.ProgressService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

open class VideoProgressController {
    @GetMapping("/progress/{guid}")
    open fun allProgressOnGuid(@PathVariable guid: String): List<BaseProgress> {
        return VideoProgressLogic.Get().getProgressOnGuid(guid)
    }

    @GetMapping("/progress/{guid}/movie")
    open fun allProgressOnGuidForMovies(@PathVariable guid: String): List<ProgressMovie> {
        return VideoProgressLogic.Get().getProgressOnGuidForMovies(guid)
    }

    @GetMapping("/progress/{guid}/serie")
    open fun allProgressOnGuidForSerie(@PathVariable guid: String): List<ProgressSerie> {
        return VideoProgressLogic.Get().getProgressOnGuidForSeries(guid)
    }

    @GetMapping("/progress/{guid}/after/{time}")
    open fun allProgressOnGuidAfterTime(@PathVariable guid: String, time: Int): List<BaseProgress> {
        return VideoProgressLogic.Get().getProgressOnGuidAfter(guid, time)
    }


    @GetMapping("/progress/{guid}/movie/{title}")
    open fun getProgressForUserWithMovieTitle(@PathVariable guid: String, @PathVariable title: String): ProgressMovie? {
        return VideoProgressLogic.Get().getProgressOnGuidWithMovieTitle(guid, title)
    }

    @GetMapping("/progress/{guid}/serie/{title}")
    open fun getProgressForUserWithSerieTitle(@PathVariable guid: String, @PathVariable title: String): ProgressSerie? {
        return VideoProgressLogic.Get().getProgressOnGuidWithSerieTitle(guid, title)
    }

    @GetMapping("/progress/{guid}/continue")
    open fun getContinue(@PathVariable guid: String): List<BaseCatalog> {
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
    open fun uploadedProgressMovie(@RequestBody progress: ProgressMovie) : ResponseEntity<String> {
        return VideoProgressLogic.Post().updateOrInsertProgressForMovie(progress)
    }

    @PostMapping("/progress/serie")
    @ResponseStatus(HttpStatus.OK)
    open fun uploadedProgressSerie(@RequestBody progress: ProgressSerie): ResponseEntity<String> {
        return VideoProgressLogic.Post().updateOrInsertProgressForSerie(progress)
    }


    @RestController
    @RequestMapping(path = ["/open"])
    class Open: VideoProgressController() {

    }


    @RestController
    @RequestMapping(path = ["/secure"])
    class Secure: VideoProgressController() {

        @Authentication(AuthenticationModes.SOFT)
        override fun allProgressOnGuid(@PathVariable guid: String): List<BaseProgress> {
            return super.allProgressOnGuid(guid)
        }

        @Authentication(AuthenticationModes.SOFT)
        override fun allProgressOnGuidForMovies(@PathVariable guid: String): List<ProgressMovie> {
            return super.allProgressOnGuidForMovies(guid)
        }

        @Authentication(AuthenticationModes.SOFT)
        override fun allProgressOnGuidForSerie(@PathVariable guid: String): List<ProgressSerie> {
            return super.allProgressOnGuidForSerie(guid)
        }

        @Authentication(AuthenticationModes.SOFT)
        override fun allProgressOnGuidAfterTime(guid: String, time: Int): List<BaseProgress> {
            return super.allProgressOnGuidAfterTime(guid, time)
        }

        @Authentication(AuthenticationModes.SOFT)
        override fun getProgressForUserWithMovieTitle(@PathVariable guid: String, @PathVariable title: String): ProgressMovie? {
            return super.getProgressForUserWithMovieTitle(guid, title)
        }

        @Authentication(AuthenticationModes.SOFT)
        override fun getProgressForUserWithSerieTitle(@PathVariable guid: String, @PathVariable title: String): ProgressSerie? {
            return super.getProgressForUserWithSerieTitle(guid, title)
        }

        @Authentication(AuthenticationModes.SOFT)
        override fun getContinue(@PathVariable guid: String): List<BaseCatalog> {
            return super.getContinue(guid)
        }

        /**
         * Post mapping below
         **/

        @Authentication(AuthenticationModes.STRICT)
        override fun uploadedProgressMovie(@RequestBody progress: ProgressMovie) : ResponseEntity<String> {
            return super.uploadedProgressMovie(progress)
        }

        @Authentication(AuthenticationModes.STRICT)
        override fun uploadedProgressSerie(@RequestBody progress: ProgressSerie): ResponseEntity<String> {
            return super.uploadedProgressSerie(progress)
        }
    }


}