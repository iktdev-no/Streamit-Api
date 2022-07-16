package no.iktdev.streamit.api.classes

import no.iktdev.streamit.api.database.data_audio
import no.iktdev.streamit.api.database.data_video
import org.jetbrains.exposed.sql.ResultRow

data class StreamData(
    val video: VideoData?,
    val audio: AudioData?
)


data class VideoData(
    val codec: String,
    val pixelFormat: String,
    val colorSpace: String?
) {
    companion object
    {
        fun fromRow(row: ResultRow) = VideoData(
            codec =  row[data_video.codec],
            pixelFormat = row[data_video.pixelFormat],
            colorSpace = row[data_video.colorSpace]
        )
    }
}

data class AudioData(
    val codec: String,
    val layout: String?,
    val language: String
) {
    companion object
    {
        fun fromRow(row: ResultRow) = AudioData(
            codec =  row[data_audio.codec],
            layout = row[data_audio.layout],
            language = row[data_audio.language]
        )
    }
}