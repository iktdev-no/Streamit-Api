package no.iktdev.streamit.api.controllers

import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.Message
import com.google.gson.Gson
import mu.KotlinLogging
import no.iktdev.streamit.api.classes.fcm.FCMBase
import no.iktdev.streamit.api.classes.fcm.FCMRemoteServerSetup
import no.iktdev.streamit.api.classes.fcm.FCMRemoteUserSetup
import no.iktdev.streamit.api.controllers.annotations.Authentication
import no.iktdev.streamit.api.controllers.annotations.AuthenticationModes
import no.iktdev.streamit.api.services.RemoteDeviceNotificationService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

val log = KotlinLogging.logger {}
open class DeviceNotificationController(open var service: RemoteDeviceNotificationService? = null) {

    open fun notifyDeviceOfIncomingMessages(@RequestBody data: FCMBase) {

    }

    @PostMapping("/configure-server")
    open fun sendConfigurationForServer(@RequestBody  data: FCMRemoteServerSetup) {
        val message = Message.builder()
            .putData("action", "no.iktdev.streamit.messaging.ConfigureServer")
            .putData("server", Gson().toJson(data.payload))

            .setToken(data.fcmReceiverId)
            .build()
        if (service == null || service?.firebaseApp == null) {
            log.error { "Service/FirebaseApp is null" }
        }
        service?.firebaseApp?.let { app ->
            FirebaseMessaging.getInstance(app).send(message)
            log.info { "Sending requested payload on 'configure-server' to FCM for ${data.fcmReceiverId}" }
        }
    }

    @PostMapping("/configure-user")
    open fun sendConfigurationForUser(@RequestBody data: FCMRemoteUserSetup) {
        val message = Message.builder()
            .putData("action", "no.iktdev.streamit.messaging.ConfigureUser")
            .putData("user", Gson().toJson(data.payload))
            .setToken(data.fcmReceiverId)
            .build()

        if (service == null || service?.firebaseApp == null) {
            log.error { "Service/FirebaseApp is null" }
        }
        service?.firebaseApp?.let { app ->
            FirebaseMessaging.getInstance(app).send(message)
            log.info { "Sending requested payload to 'configure-user' on FCM for ${data.fcmReceiverId}" }
        }
    }



    @RestController
    @RequestMapping(path = ["/secure/device-notification"])
    class Secure(@Autowired override var service: RemoteDeviceNotificationService? = null): DeviceNotificationController() {

        @Authentication(AuthenticationModes.STRICT)
        override fun notifyDeviceOfIncomingMessages(@RequestBody data: FCMBase) {
            super.notifyDeviceOfIncomingMessages(data)
        }

        @Authentication(AuthenticationModes.STRICT)
        override fun sendConfigurationForServer(@RequestBody data: FCMRemoteServerSetup) {
            super.sendConfigurationForServer(data)
        }

        @Authentication(AuthenticationModes.STRICT)
        override fun sendConfigurationForUser(@RequestBody data: FCMRemoteUserSetup) {
            super.sendConfigurationForUser(data)
        }


    }


    @RestController
    @RequestMapping(path = ["/open/device-notification"])
    class Open(@Autowired override var service: RemoteDeviceNotificationService? = null): DeviceNotificationController() {
        override fun notifyDeviceOfIncomingMessages(@RequestBody data: FCMBase) {
            super.notifyDeviceOfIncomingMessages(data)
        }

        override fun sendConfigurationForServer(@RequestBody data: FCMRemoteServerSetup) {
            super.sendConfigurationForServer(data)
        }

        override fun sendConfigurationForUser(@RequestBody data: FCMRemoteUserSetup) {
            super.sendConfigurationForUser(data)
        }

    }
}