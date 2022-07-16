package no.iktdev.streamit.api.database

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.timestamp
import java.sql.Time
import java.sql.Timestamp
import java.time.Instant

object catalog : IntIdTable() {
    val title: Column<String> = varchar("title", 250).uniqueIndex()
    var cover: Column<String> = varchar("cover", 250)
    var type: Column<String> = varchar("type", 50)
    var collection: Column<String> = varchar("collection", 100)
    var iid: Column<Int> = integer("iid")
    var genres: Column<String> = varchar("genres", 24)
    val added: Column<Instant> = timestamp("added")
}

object genre: IntIdTable() {
    val genre: Column<String> = varchar("genre", 50).uniqueIndex()
}

object movie: IntIdTable() {
    val video: Column<String> = varchar("video", 100).uniqueIndex()
}

object serie: IntIdTable() {
    val title: Column<String> = varchar("title", 250)
    val episode: Column<Int> = integer("episode")
    val season: Column<Int> = integer("season")
    val collection: Column<String> = varchar("collection", 250)
    val video: Column<String> = varchar("video", 100).uniqueIndex()
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
    val collection: Column<String> = varchar("collection", 250)
    val episode: Column<Int> = integer("episode")
    val season: Column<Int> = integer("season")
    val video: Column<String> = varchar("video", 100)
    val progress: Column<Int> = integer("progress")
    val duration: Column<Int> = integer("duration")
    val played: Column<Int> = integer("played")
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