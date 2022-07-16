package no.iktdev.streamit.api.controllers

import no.iktdev.streamit.api.classes.Heartbeat
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import javax.servlet.http.HttpServletResponse

@RestController
@RequestMapping(path = ["/secure"])
class GeneralSecureController {

    @GetMapping(value = ["/", "/heartbeat"])
    fun heartbeatPath(): Heartbeat {
        return Heartbeat(true, System.currentTimeMillis() / 1000L)
    }


    @GetMapping("/swagger")
    fun swaggerRedirect(response: HttpServletResponse): ResponseEntity<String> {
        return ResponseEntity("Unavailable through secure endpoint.\n Please perform this request on local net!", HttpStatus.SERVICE_UNAVAILABLE)
    }
}