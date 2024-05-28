import com.google.gson.Gson
import no.iktdev.streamit.api.classes.Genre
import no.iktdev.streamit.api.classes.Movie
import no.iktdev.streamit.api.classes.Subtitle
import no.iktdev.streamit.api.classes.Summary
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.assertDoesNotThrow
import java.io.Serializable

class TestSerialization {

    @Test
    fun testSerialization() {
        val movie = Movie(
            0,
            "Potato",
            "Potato.cover",
            "Potato",
            "",
            true,
            "Potato.mp4",
            0,
            0,
            0,
            emptyList()
        )

        assertDoesNotThrow {

            val movieJson = Gson().toJson(movie)
            val movieJ = Gson().fromJson<Movie>(movieJson, Movie::class.java)
            assertThat(movieJ).isNotNull()
        }
    }

}