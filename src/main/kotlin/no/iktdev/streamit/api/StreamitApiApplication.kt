package no.iktdev.streamit.api

import no.iktdev.streamit.api.database.DataSource
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.ApplicationContext

@SpringBootApplication
class StreamitApiApplication

private var context: ApplicationContext? = null

fun main(args: Array<String>) {
	val ds = DataSource().getConnection()
	System.out.println(ds)

	context = runApplication<StreamitApiApplication>(*args)
}

fun getContext(): ApplicationContext? {
	return context
}

fun Log(c: Class<*>, message: String) {
	val caller: String = c::class.java.simpleName
	LoggerFactory.getLogger(caller).info(message)
}

fun Log(c: Class<*>): Logger {
	val caller: String = c::class.java.simpleName
	return LoggerFactory.getLogger(caller)
}
