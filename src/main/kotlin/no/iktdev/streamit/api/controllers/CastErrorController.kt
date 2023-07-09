package no.iktdev.streamit.api.controllers

import com.google.gson.Gson
import no.iktdev.streamit.api.classes.*
import no.iktdev.streamit.api.controllers.annotations.Authentication
import no.iktdev.streamit.api.controllers.annotations.AuthenticationModes
import no.iktdev.streamit.api.database.queries.QCastError
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

open class CastErrorController {

    /**
    * Post mapping below
    **/


    @PostMapping("/error/cast")
    @ResponseStatus(HttpStatus.OK)
    open fun uploadedCastError(@RequestBody data: CastError) : ResponseEntity<String> {
        QCastError().insertCastError(data)
        return ResponseEntity.ok(Gson().toJson(Response()))
    }


    @RestController
    @RequestMapping(path = ["/open"])
    class OpenProgress: CastErrorController()

    @RestController
    @RequestMapping(path = ["/secure"])
    class RestrictedProgress: CastErrorController() {

        /**
         * Post mapping below
         **/
        @Authentication(AuthenticationModes.SOFT)
        override fun uploadedCastError(@RequestBody data: CastError): ResponseEntity<String> {
            return super.uploadedCastError(data)
        }
    }


}