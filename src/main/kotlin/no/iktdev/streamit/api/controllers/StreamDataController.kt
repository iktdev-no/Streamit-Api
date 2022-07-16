package no.iktdev.streamit.api.controllers

import no.iktdev.streamit.api.classes.AudioData
import no.iktdev.streamit.api.classes.StreamData
import no.iktdev.streamit.api.classes.VideoData
import no.iktdev.streamit.api.database.data_audio
import no.iktdev.streamit.api.database.data_video
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController

/**
 *
 * CURRENTLY NOT IN USE!
 *
 * **/

@RestController
class StreamDataController {

    @GetMapping("/data/stream/{file}")
    fun getStreamData(@PathVariable file: String?): StreamData? {
        return StreamData(getVideoStream(file), getAudioStream(file))
    }


    @GetMapping("/data/stream/audio/{file}")
    fun getAudioStream(@PathVariable file: String? = null): AudioData?
    {
        var audioData: AudioData? = null
        if (file.isNullOrEmpty()) return null
        transaction {
            val data = data_audio.select { data_audio.file eq file }.singleOrNull()
            audioData = data?.let { AudioData.fromRow(it) }
        }
        return audioData
    }

    @GetMapping("/data/stream/video/{file}")
    fun getVideoStream(@PathVariable file: String? = null): VideoData?
    {
        var videodata: VideoData? = null
        if (file.isNullOrEmpty()) return null
        transaction {
            val data = data_video.select { data_video.file eq file }.singleOrNull()
            videodata = data?.let { VideoData.fromRow(it) }
        }
        return videodata
    }

}