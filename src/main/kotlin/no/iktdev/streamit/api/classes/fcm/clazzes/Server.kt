package no.iktdev.streamit.api.classes.fcm.clazzes

import java.io.Serializable

data class Server(val id: String, var name: String, var fingerprint: String?, val lan: String, val remote: String? = null, var remoteSecure: Boolean = false):
    Serializable