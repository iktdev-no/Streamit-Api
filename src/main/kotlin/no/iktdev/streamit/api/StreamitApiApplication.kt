package no.iktdev.streamit.api

import no.iktdev.streamit.api.database.DataSource
import no.iktdev.streamit.api.database.cast_errors
import no.iktdev.streamit.api.database.tables
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

	context = runApplication<StreamitApiApplication>(*args)

	transaction {
		SchemaUtils.createMissingTablesAndColumns(*tables)
	}
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
