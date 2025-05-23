package no.iktdev.streamit.api.controllers

import com.google.gson.Gson
import no.iktdev.streamit.api.classes.*
import no.iktdev.streamit.api.controllers.annotations.Authentication
import no.iktdev.streamit.api.controllers.annotations.AuthenticationModes
import no.iktdev.streamit.api.controllers.logic.VideoProgressLogic
import no.iktdev.streamit.api.database.queries.QProgress
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

open class VideoProgressController {
    @GetMapping("/progress/{guid}")
    open fun allProgressOnGuid(@PathVariable guid: String): List<BaseProgress> {
        return VideoProgressLogic.Get().getProgressOnGuid(guid)
    }

    @GetMapping(value = [
        "/progress/{guid}/movie",
        "/progress/{guid}/movie/after/{time}"
    ])
    open fun allProgressOnGuidForMovies(@PathVariable guid: String, @PathVariable time: Int? = null): List<ProgressMovie> {
        return if (time == null) VideoProgressLogic.Get().getProgressOnGuidForMovies(guid) else
            VideoProgressLogic.Get().getProgressOnGuidForMovieAfter(guid, time)
    }

    @GetMapping(value = [
        "/progress/{guid}/serie",
        "/progress/{guid}/serie/after/{time}"
    ])
    open fun allProgressOnGuidForSerie(@PathVariable guid: String, @PathVariable time: Int? = null): List<ProgressSerie> {
        return if (time == null) VideoProgressLogic.Get().getProgressOnGuidForSeries(guid) else
            VideoProgressLogic.Get().getProgressOnGuidSerieAfter(guid, time)
    }

    @GetMapping("/progress/{guid}/movie/{title}")
    open fun getProgressForUserWithMovieTitle(@PathVariable guid: String, @PathVariable title: String): ProgressMovie? {
        return VideoProgressLogic.Get().getProgressOnGuidWithMovieTitle(guid, title)
    }

    @GetMapping("/progress/{guid}/serie/{title}")
    open fun getProgressForUserWithSerieTitle(@PathVariable guid: String, @PathVariable title: String): ProgressSerie? {
        return VideoProgressLogic.Get().getProgressOnGuidWithSerieTitle(guid, title)
    }

    /*@GetMapping("/progress/{guid}/continue")
    open fun getContinue(@PathVariable guid: String): List<BaseCatalog> {
        return VideoProgressLogic.Get().getContinueOnForGuid(guid)
    }*/

    @GetMapping("/progress/{guid}/continue/serie")
    open fun getContinueSerie(@PathVariable guid: String): List<Serie> {
        return VideoProgressLogic.Get().getContinueSerieOnForGuid(guid)
    }

    /**
    * Post mapping below
    **/


    @PostMapping("/progress/{guid}/movie")
    @ResponseStatus(HttpStatus.OK)
    open fun uploadedProgressMovieOnGuid(@PathVariable guid: String, @RequestBody progress: Movie) : ResponseEntity<String> {
        QProgress().upsertMovieOnGuid(guid, progress)
        return ResponseEntity.ok(Gson().toJson(Response()))
    }

    @PostMapping("/progress/{guid}/serie")
    @ResponseStatus(HttpStatus.OK)
    open fun uploadedProgressSerieOnGuid(@PathVariable guid: String, @RequestBody progress: Serie): ResponseEntity<String> {
        QProgress().upsertSerieOnGuid(guid, progress)
        return ResponseEntity.ok(Gson().toJson(Response()))
    }


    @RestController
    @RequestMapping(path = ["/open"])
    class OpenProgress: VideoProgressController()


    @RestController
    @RequestMapping(path = ["/secure"])
    class RestrictedProgress: VideoProgressController() {

        @Authentication(AuthenticationModes.SOFT)
        override fun allProgressOnGuid(@PathVariable guid: String): List<BaseProgress> {
            return super.allProgressOnGuid(guid)
        }

        @Authentication(AuthenticationModes.SOFT)
        override fun allProgressOnGuidForMovies(@PathVariable guid: String, @PathVariable time: Int?): List<ProgressMovie> {
            return super.allProgressOnGuidForMovies(guid, time)
        }

        @Authentication(AuthenticationModes.SOFT)
        override fun allProgressOnGuidForSerie(@PathVariable guid: String, @PathVariable time: Int?): List<ProgressSerie> {
            return super.allProgressOnGuidForSerie(guid, time)
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
        override fun getContinueSerie(@PathVariable guid: String): List<Serie> {
            return super.getContinueSerie(guid)
        }

        /**
         * Post mapping below
         **/
        @Authentication(AuthenticationModes.STRICT)
        override fun uploadedProgressMovieOnGuid(@PathVariable guid: String, @PathVariable progress: Movie): ResponseEntity<String> {
            return super.uploadedProgressMovieOnGuid(guid, progress)
        }
        @Authentication(AuthenticationModes.STRICT)

        override fun uploadedProgressSerieOnGuid(@PathVariable guid: String, @PathVariable progress: Serie): ResponseEntity<String> {
            return super.uploadedProgressSerieOnGuid(guid, progress)
        }
    }


}