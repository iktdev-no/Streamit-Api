package no.iktdev.streamitapi.controllers

import no.iktdev.streamitapi.classes.Heartbeat
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import javax.servlet.http.HttpServletResponse


@RestController
class HeartbeatController {

    private fun heartbeat(): Heartbeat
    {
        return Heartbeat(true, System.currentTimeMillis() / 1000L)
    }

    @GetMapping("/heartbeat")
    fun heartbeatPath(): Heartbeat
    {
        return heartbeat()
    }

    @GetMapping("/")
    fun defaultPath(): Heartbeat
    {
        return heartbeat()
    }

    @GetMapping("/swagger")
    fun swaggerRedirect(response: HttpServletResponse) {
        response.setHeader("Location", "/swagger-ui.html")
        response.status = 302
    }

}