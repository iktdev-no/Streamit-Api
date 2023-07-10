package no.iktdev.streamit.api.controllers.annotations

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class Authentication(val mode: AuthenticationModes = AuthenticationModes.SOFT)

enum class AuthenticationModes {
    STRICT,
    SOFT,
    NONE
}