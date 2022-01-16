package no.iktdev.streamitapi

import no.iktdev.streamitapi.database.DataSource
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
