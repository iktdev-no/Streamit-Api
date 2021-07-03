package no.iktdev.streamitapi.controllers

import no.iktdev.streamitapi.classes.Heartbeat
import org.jetbrains.exposed.sql.Database
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

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

}