package no.iktdev.streamit.api.classes

data class CastError(
    val file: String,
    val deviceModel: String,
    val deviceManufacturer: String,
    val deviceBrand: String,
    val deviceAndroidVersion: String,
    val appVersion: String,
    val castDeviceName: String,
    val error: String
)
