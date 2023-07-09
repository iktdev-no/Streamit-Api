package no.iktdev.streamit.api.controllers

import no.iktdev.streamit.api.classes.Heartbeat
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import javax.servlet.http.HttpServletResponse

open class GeneralController {

    @GetMapping(value = ["/", "/heartbeat"])
    open fun heartbeatPath(): Heartbeat {
        return Heartbeat(true, System.currentTimeMillis() / 1000L)
    }


   /* @GetMapping("/swagger")
    open fun swaggerRedirect(response: HttpServletResponse) {
        response.setHeader("Location", "/open/swagger-ui.html")
        response.status = 302
    }*/

    @RestController
    @RequestMapping(path = ["/open"])
    class Open : GeneralController()

    @RestController
    @RequestMapping(path = ["/secure"])
    class Secure: GeneralController() {

        /*@GetMapping("/swagger")
        override fun swaggerRedirect(response: HttpServletResponse) {
            response.status = HttpStatus.SERVICE_UNAVAILABLE.value()
            response.sendError(response.status, "Unavailable through secure endpoint.\n Please perform this request on local net!")
        }*/
    }


}