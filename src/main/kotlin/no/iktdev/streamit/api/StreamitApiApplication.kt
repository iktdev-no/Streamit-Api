package no.iktdev.streamit.api

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import kotlinx.coroutines.launch
import mu.KotlinLogging
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
import java.io.File
import java.io.FileInputStream

@SpringBootApplication
class StreamitApiApplication

private var context: ApplicationContext? = null
val log = KotlinLogging.logger {}

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
			resumeOrNext,
			registeredDevices,
			delegatedAuthenticationTable
		)
		transaction {
			SchemaUtils.createMissingTablesAndColumns(*tables)
			log.info("Database transaction completed")
		}
	}
	context = runApplication<StreamitApiApplication>(*args)
}


fun getContext(): ApplicationContext? {
	return context
}
