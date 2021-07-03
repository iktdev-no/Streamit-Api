package no.iktdev.streamitapi

import no.iktdev.streamitapi.database.DataSource
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class StreamitApiApplication

fun main(args: Array<String>) {
	val ds = DataSource().getConnection()
	System.out.println(ds)
	runApplication<StreamitApiApplication>(*args)
}
