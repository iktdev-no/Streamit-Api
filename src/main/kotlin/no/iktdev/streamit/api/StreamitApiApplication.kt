package no.iktdev.streamit.api

import kotlinx.coroutines.launch
import no.iktdev.streamit.api.helper.Coroutines
import no.iktdev.streamit.library.db.datasource.MySqlDataSource
import no.iktdev.streamit.library.db.tables.*
import no.iktdev.streamit.library.db.tables.helper.cast_errors
import no.iktdev.streamit.library.db.tables.helper.data_audio
import no.iktdev.streamit.library.db.tables.helper.data_video
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
	val ds = MySqlDataSource.fromDatabaseEnv().createDatabase()
	System.out.println(ds)

	Coroutines().Coroutine().launch {
		val tables = arrayOf(
			catalog,
			genre,
			movie,
			serie,
			subtitle,
			summary,
			users,
			progress,
			data_audio,
			data_video,
			cast_errors,
			resumeOrNext
		)
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
