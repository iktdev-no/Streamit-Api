package no.iktdev.streamit.api.controllers

import no.iktdev.streamit.api.classes.Heartbeat
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import javax.servlet.http.HttpServletResponse

@RestController
@RequestMapping(path = ["/open"])
class GeneralOpenController {

    @GetMapping(value = ["/", "/heartbeat"])
    fun heartbeatPath(): Heartbeat {
        return Heartbeat(true, System.currentTimeMillis() / 1000L)
    }


    @GetMapping("/swagger")
    fun swaggerRedirect(response: HttpServletResponse) {
        response.setHeader("Location", "/swagger-ui.html")
        response.status = 302
    }
}