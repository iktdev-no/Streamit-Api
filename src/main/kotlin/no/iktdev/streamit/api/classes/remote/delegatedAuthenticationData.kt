package no.iktdev.streamit.api.classes.remote

import java.time.LocalDateTime

data class delegatedAuthenticationData(
    val pin: String,
    val requesterId: String,
    val created: LocalDateTime,
    val expires: LocalDateTime
)