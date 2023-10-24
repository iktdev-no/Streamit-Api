package no.iktdev.streamit.api.database.queries

import no.iktdev.streamit.api.classes.Subtitle
import no.iktdev.streamit.library.db.tables.subtitle
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.andWhere
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction

class QSubtitle {

    fun selectSubtitlBasedOnTitleOrVideo(title: String, format: String? = null): List<Subtitle> {
        var baseName = title
        if (title.contains("."))
            baseName = title.substring(0, title.lastIndexOf("."))

        return transaction {
            val result = if (format.isNullOrEmpty())
                subtitle.select { subtitle.associatedWithVideo eq baseName }
            else
                subtitle.select { subtitle.associatedWithVideo eq baseName }
                    .andWhere { subtitle.format eq format }
            result.mapNotNull { Subtitle.fromRow(it) }
        }
    }

    fun selectSubtitleForVideo(video: String, format: String? = null): List<Subtitle> {
        val videoBaseName = video.substringBeforeLast(".")

        return transaction {
            val result = if (format.isNullOrEmpty())
                subtitle.select { subtitle.associatedWithVideo eq videoBaseName }
            else
                subtitle.select { subtitle.associatedWithVideo eq videoBaseName }
                    .andWhere { subtitle.format eq format }
            result.mapNotNull { Subtitle.fromRow(it) }
        }
    }

    fun selectSubtitleForSerieEpisode(collection: String, video: String, format: String? = null): List<Subtitle> {
        val videoBaseName = video.substringBeforeLast(".")
        return transaction {
            val result = if (format.isNullOrBlank())
                subtitle.select { subtitle.collection eq collection }
                    .andWhere { subtitle.associatedWithVideo eq videoBaseName }
            else
                subtitle.select { subtitle.collection eq collection }
                    .andWhere { subtitle.format eq format }
                    .andWhere { subtitle.associatedWithVideo eq videoBaseName }
            result.mapNotNull { Subtitle.fromRow(it) }
        }
    }

    fun selectSubtitleForMovie(collection: String, video: String, format: String? = null): List<Subtitle> {
        val videoBaseName = video.substringBeforeLast(".")
        return transaction {
            val result = if (format.isNullOrBlank())
                subtitle.select { subtitle.collection eq collection }
                    .andWhere { subtitle.associatedWithVideo eq videoBaseName }
            else
                subtitle.select { subtitle.collection eq collection }
                    .andWhere { subtitle.format eq format }
                    .andWhere { subtitle.associatedWithVideo eq videoBaseName }
            result.mapNotNull { Subtitle.fromRow(it) }
        }
    }


    fun deleteSubtitleOnId(id: Int): Boolean {
        return transaction {
            val rows = subtitle.deleteWhere {
                this.id.eq(id)
            }
            if (rows > 1) {
                rollback()
                false
            } else
                true
        }
    }

}