package no.iktdev.streamit.api.controllers

import no.iktdev.streamit.api.classes.RegisterDeviceData
import no.iktdev.streamit.api.controllers.annotations.Authentication
import no.iktdev.streamit.api.controllers.annotations.AuthenticationModes
import no.iktdev.streamit.library.db.executeWithStatus
import no.iktdev.streamit.library.db.tables.registeredDevices
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.update
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

open class DeviceRegistrationController {

    @PostMapping("/register")
    open fun register(@RequestBody device: RegisterDeviceData): ResponseEntity<String> {
        val status = executeWithStatus {
            registeredDevices.insert {
                it[deviceId] = device.deviceId
                it[applicationPackageName] = device.applicationPackageName
                it[osVersion] = device.osVersion
                it[osPlatform] = device.osPlatform
            }
        }
        if (status) {
            return ResponseEntity.ok().build()
        }
        return ResponseEntity.unprocessableEntity().build()
    }

    @PostMapping("/replace/{oldToken}/set")
    open fun registerNewToken(@PathVariable oldToken: String, @RequestBody newToken: String): ResponseEntity<String> {
        val status = executeWithStatus {
            registeredDevices.update({registeredDevices.deviceId eq oldToken}) {
                it[deviceId] = newToken
            }
        }
        if (status) {
            return ResponseEntity.ok().build()
        }
        return ResponseEntity.badRequest().build()
    }


    @GetMapping("/list")
    open fun getRegisteredDevices(): ResponseEntity<String> {
        return ResponseEntity.noContent().build()
    }


    @RestController
    @RequestMapping(path = ["/open/device-registration"])
    class OpenDeviceRegistration: DeviceRegistrationController() {
        override fun getRegisteredDevices(): ResponseEntity<String> {
            return super.getRegisteredDevices()
        }
    }

    @RestController
    @RequestMapping(path = ["/secure/device-registration"])
    class RestrictedDeviceRegistration: DeviceRegistrationController() {

        @Authentication(AuthenticationModes.STRICT)
        override fun getRegisteredDevices(): ResponseEntity<String> {
            return super.getRegisteredDevices()
        }
    }

}