package no.iktdev.streamit.api.database.queries

import no.iktdev.streamit.api.classes.Subtitle
import no.iktdev.streamit.api.database.movie
import no.iktdev.streamit.api.database.subtitle
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.andWhere
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction

class QSubtitle {

    fun selectSubtitleForVideo(title: String, format: String? = null): List<Subtitle> {
        var baseName = title
        if (title.contains("."))
            baseName = title.substring(0, title.lastIndexOf("."))

        return transaction {
            val result = if (format.isNullOrEmpty())
                subtitle.select { subtitle.title eq baseName }
            else
                subtitle.select { subtitle.title eq baseName }
                    .andWhere { subtitle.format eq format }
            result.mapNotNull { Subtitle.fromRow(it) }
        }
    }

    fun selectSubtitlesForSerie(collection: String, format: String?): List<Subtitle> {
        return transaction {
            val result = if (format.isNullOrEmpty())
                subtitle.select { subtitle.collection eq collection }
            else
                subtitle.select { subtitle.collection eq collection }
                    .andWhere { subtitle.format eq format }
            result.mapNotNull { Subtitle.fromRow(it) }
        }
    }

    fun deleteSubtitleOnId(id: Int): Boolean {
        return transaction {
            val rows = subtitle.deleteWhere { subtitle.id eq id }
            if (rows > 1) {
                rollback()
                false
            } else
                true
        }
    }

}