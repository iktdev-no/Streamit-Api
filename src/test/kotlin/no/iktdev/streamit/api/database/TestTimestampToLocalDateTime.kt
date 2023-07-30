package no.iktdev.streamit.api.database

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

class TestTimestampToLocalDateTime {
    @Test
    fun `test timestampToLocalDateTime`() {
        val timestamp = 1640908800 // 2021-12-31T00:00:00Z
        val expected = LocalDateTime.of(2021, 12, 31, 1, 0, 0)
        assertEquals(expected, timestampToLocalDateTime(timestamp))
    }

    @Test
    fun `test LocalDateTime toEpochSeconds`() {
        val localDateTime = LocalDateTime.of(2021, 12, 31, 1, 0, 0)
        val expected = 1640908800L // 2021-12-31T00:00:00Z
        assertEquals(expected, localDateTime.toEpochSeconds())
    }
}