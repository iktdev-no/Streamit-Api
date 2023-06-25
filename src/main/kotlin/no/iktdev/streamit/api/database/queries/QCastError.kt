package no.iktdev.streamit.api.database.queries

import no.iktdev.streamit.api.classes.CastError
import no.iktdev.streamit.api.database.cast_errors
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.transactions.transaction

class QCastError {
    fun insertCastError(castError: CastError) {
        return transaction {
            cast_errors.insertAndGetId {
                it[this.deviceAndroidVersion] = castError.deviceAndroidVersion
                it[castDeviceName] = castError.castDeviceName
                it[appVersion] = castError.appVersion
                it[file] = castError.file
                it[deviceBrand] = castError.deviceBrand
                it[deviceModel] = castError.deviceModel
                it[deviceManufacturer] = castError.deviceManufacturer
            }.value
        }
    }
}