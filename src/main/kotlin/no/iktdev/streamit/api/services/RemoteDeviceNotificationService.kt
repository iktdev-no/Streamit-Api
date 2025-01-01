package no.iktdev.streamit.api.services

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import no.iktdev.streamit.api.Configuration
import no.iktdev.streamit.api.SupportedFeatures
import no.iktdev.streamit.api.log
import org.springframework.stereotype.Service
import java.io.File
import java.io.FileInputStream

@Service
class RemoteDeviceNotificationService {
    var firebaseApp: FirebaseApp? = null
    init {
        val fcmFile = Configuration.firebaseServiceFile?.let {
            File(it)
        }
        if (fcmFile == null || !fcmFile.exists()) {
            log.warn { "No firebase service file found." }
            log.warn { "FCM provided notifications will not be available, and remote configuration and sharing will be unavailable." }
        } else {
            try {
                FileInputStream(fcmFile.absolutePath).use { fis ->
                    val options = FirebaseOptions.builder()
                        .setCredentials(GoogleCredentials.fromStream(fis))
                        .build()
                    firebaseApp = FirebaseApp.initializeApp(options)
                    if (firebaseApp != null) {
                        firebaseApp?.let {
                            log.info { "Created and configured FCM instance ${it.name}" }
                        }
                    } else {
                        log.error { "Failed to configure FCM Instance" }
                    }
                    SupportedFeatures.supportsRemoteDeviceConfiguration = true
                }
            } catch (e: Exception) {
                e.printStackTrace()
                log.error { "Failed to configure FCM Instance" }
            }
        }
    }



}