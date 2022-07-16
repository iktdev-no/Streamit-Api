package no.iktdev.streamit.api.helper

import no.iktdev.streamit.api.Configuration
import java.time.LocalDateTime

class timeParse {

    fun recentTime(time: String): LocalDateTime {
        var recentAdded = LocalDateTime.now()
        recentAdded = when {
            time.contains("d") -> {
                val days = time.trim('d').toLong()
                recentAdded.minusDays(days)
            }
            time.contains("m") -> {
                val months = time.trim('m').toLong()
                recentAdded.minusMonths(months)
            }
            time.contains("y") -> {
                val years = time.trim('y').toLong()
                recentAdded.minusYears(years)
            }
            else -> {
                recentAdded.minusDays(Configuration.frshness*3)
            }
        }
        return recentAdded
    }

    fun configTime(time: String?): LocalDateTime {
        var current = LocalDateTime.now()
        when {
            time.isNullOrEmpty() || time.lowercase() == "0d" -> {
                current = current.plusDays(30) // jwt is set to live for 30 days
            }
            else -> {
                current = when {
                    time.contains("h") -> {
                        val hours = time.trim('h').toLong()
                        current.plusHours(hours)
                    }
                    time.contains("d") -> {
                        val days = time.trim('d').toLong()
                        current.plusDays(days)
                    }
                    time.contains("m") -> {
                        val months = time.trim('m').toLong()
                        current.plusMonths(months)
                    }
                    time.contains("y") -> {
                        val years = time.trim('y').toLong()
                        current.plusYears(years)
                    }
                    else -> current.plusDays(30)
                }
            }
        }
        return current
    }
}