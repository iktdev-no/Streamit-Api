package no.iktdev.streamit.api.controllers.logic

import no.iktdev.streamit.api.classes.Subtitle
import no.iktdev.streamit.api.database.queries.QSubtitle
import no.iktdev.streamit.library.db.tables.subtitle
import org.jetbrains.exposed.sql.andWhere
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction

class SubtitleLogic {

    /**
     * Can be used for both serie(episode) and movie
     * Only a single set of subtitles will be returned
     *
     * @param title BaseName of movie
     * @param format VTT,SRT,SMI,ASS
     */
    fun videoSubtitle(title: String, format: String?): List<Subtitle> {
        return QSubtitle().selectSubtitlBasedOnTitleOrVideo(title, format)
    }

    /**
     * Can only be used for serie
     *
     * @param collection BaseName of movie
     * @param format VTT,SRT,SMI,ASS
     */
    fun serieSubtitle(collection: String, format: String?): List<Subtitle> {
        return transaction {
            val result = if (format.isNullOrEmpty())
                subtitle.select { subtitle.collection eq collection }
            else
                subtitle.select { subtitle.collection eq collection }
                    .andWhere { subtitle.format eq format }
            result.mapNotNull { Subtitle.fromRow(it) }
        }
    }


}