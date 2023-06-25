package no.iktdev.streamit.api

import kotlinx.coroutines.launch
import no.iktdev.streamit.api.database.DataSource
import no.iktdev.streamit.api.database.cast_errors
import no.iktdev.streamit.api.database.tables
import no.iktdev.streamit.api.helper.Coroutines
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
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

	Coroutines().Coroutine().launch {
		transaction {
			SchemaUtils.createMissingTablesAndColumns(*tables)
			Log(this::class.java).info("Database transaction completed")
		}
	}

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
