package no.iktdev.streamit.api.database

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.CurrentDateTime
import org.jetbrains.exposed.sql.javatime.datetime
import org.jetbrains.exposed.sql.javatime.timestamp
import java.sql.Time
import java.sql.Timestamp
import java.time.Instant
import java.time.LocalDateTime

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
    cast_errors
)

object catalog : IntIdTable() {
    val title: Column<String> = varchar("title", 250).uniqueIndex()
    var cover: Column<String?> = varchar("cover", 250).nullable()
    var type: Column<String> = varchar("type", 50)
    var collection: Column<String> = varchar("collection", 100)
    var iid: Column<Int?> = integer("iid").nullable()
    var genres: Column<String?> = varchar("genres", 24).nullable()
    val added: Column<Instant> = timestamp("added")
}

object genre: IntIdTable() {
    val genre: Column<String> = varchar("genre", 50).uniqueIndex()
}

object movie: IntIdTable() {
    val video: Column<String> = varchar("video", 250).uniqueIndex()
}

object serie: IntIdTable() {
    val title: Column<String> = varchar("title", 250)
    val episode: Column<Int> = integer("episode")
    val season: Column<Int> = integer("season")
    val collection: Column<String> = varchar("collection", 250)
    val video: Column<String> = varchar("video", 250).uniqueIndex()
    val added: Column<Instant> = timestamp("added")
}

object subtitle: IntIdTable() {
    val title: Column<String> = varchar("title", 250)
    val language: Column<String> = varchar("language", 16)
    val subtitle: Column<String> = varchar("subtitle", 250)
    val collection: Column<String> = varchar("collection", 250)
    val format: Column<String> = varchar("format", 12)
    val _unqfull: Column<String> = varchar("unqfull", 250).uniqueIndex()
}

object summary: IntIdTable() {
    val description: Column<String> = text("description")
    val language: Column<String> = varchar("language", 16)
    val cid: Column<Int> = integer("cid")
    val unqfull: Column<String> = varchar("unqfull", 250).uniqueIndex()
}

object users: Table() {
    val guid: Column<String> = varchar("guid", 50)
    val name: Column<String> = varchar("name", 50).uniqueIndex()
    val image: Column<String> = varchar("image", 200)
}

object progress : IntIdTable() {
    val guid: Column<String> = varchar("guid", 50)
    val type: Column<String> = varchar("type", 10)
    val title: Column<String> = varchar("title", 100)
    val collection: Column<String?> = varchar("collection", 250).nullable()
    val episode: Column<Int?> = integer("episode").nullable()
    val season: Column<Int?> = integer("season").nullable()
    val video: Column<String> = varchar("video", 100)
    val progress: Column<Int> = integer("progress")
    val duration: Column<Int> = integer("duration")
    val played: Column<Int?> = integer("played").nullable()
}

object metadata_catalog: IntIdTable() {
    val sourceId: Column<Int> = integer("sourceId")
    val metaSource: Column<String> = varchar("source", 16)
    val sourceTitle: Column<String> = varchar("sourceTitle", 200)
}

object data_video: IntIdTable() {
    val file: Column<String> = varchar("source", 200).uniqueIndex()
    val codec: Column<String> = varchar("codec", 12)
    val pixelFormat: Column<String> = varchar("pixelFormat", 12)
    val colorSpace: Column<String?> = varchar("colorSpace", 8).nullable()
}

object data_audio: IntIdTable() {
    val file: Column<String> = varchar("source", 200).uniqueIndex() // Currently Audio Stream is embedded in video file. Might change at a later date
    val codec: Column<String> = varchar("codec", 12)
    val channels: Column<Int?> = integer("channels").nullable()
    val sample_rate: Column<Int?> = integer("sampleRate").nullable()
    val layout: Column<String?> = varchar("layout", 8).nullable()
    val language: Column<String> = varchar("language", 6)
}

object cast_errors: IntIdTable() {
    val file: Column<String> = varchar("source", 200)
    val deviceModel: Column<String> = varchar("deviceModel", 50)
    val deviceManufacturer: Column<String> = varchar("deviceManufacturer", 50)
    val deviceBrand: Column<String> = varchar("deviceBrand", 50)
    val deviceAndroidVersion = varchar("androidVersion", 10)
    val appVersion = varchar("appVersion", 10)
    val castDeviceName: Column<String> = varchar("castDeviceName", 50)
    val error = text("error")
    val timestamp = datetime("timestamp").clientDefault { LocalDateTime.now() }
}
