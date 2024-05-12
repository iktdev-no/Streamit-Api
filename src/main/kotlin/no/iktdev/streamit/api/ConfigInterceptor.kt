package no.iktdev.streamit.api

import mu.KotlinLogging
import no.iktdev.streamit.api.controllers.AuthenticationInterceptor
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer


@Configuration
class ConfigInterceptor(@Autowired val authenticationInterceptor: AuthenticationInterceptor): WebMvcConfigurer {
    val log = KotlinLogging.logger {}

    override fun addInterceptors(registry: InterceptorRegistry) {
        super.addInterceptors(registry)
        log.info("Adding auth interceptor")
        registry.addInterceptor(authenticationInterceptor).addPathPatterns("/secure/**")
    }
}