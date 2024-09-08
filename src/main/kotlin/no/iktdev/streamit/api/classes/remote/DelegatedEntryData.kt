package no.iktdev.streamit.api.classes.remote

import no.iktdev.streamit.library.db.tables.AuthMethod
import java.time.LocalDateTime

data class DelegatedEntryData(
    val requesterId: String,
    val pin: String,
    val deviceInfo: DelegatedDeviceInfo
)

data class DelegatedDeviceInfo(
    val deviceName: String?,
    val deviceModel: String?,
    val deviceManufacturer: String?,
    val osVersion: String?,
    val osPlatform: String?
)

data class DelegatedRequestData(
    val requesterId: String,
    val pin: String,
    val deviceInfo: DelegatedDeviceInfo,
    val created: LocalDateTime,
    val expires: LocalDateTime,
    val permitted: Boolean,
    val consumed: Boolean,
    val method: AuthMethod
)