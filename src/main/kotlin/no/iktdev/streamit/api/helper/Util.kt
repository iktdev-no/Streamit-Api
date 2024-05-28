package no.iktdev.streamit.api.helper

fun String.withoutExtension(): String {
    return try {
        val lastIndexOfDot = this.lastIndexOf(".") ?: -1
        if (lastIndexOfDot < 0) {
            return this
        } else {
            this.substring(0, lastIndexOfDot)
        }
    } catch (e: Exception) {
        e.printStackTrace()
        this
    }
}