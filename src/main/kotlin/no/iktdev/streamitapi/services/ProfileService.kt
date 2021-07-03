package no.iktdev.streamitapi.services

import net.dzikoysk.exposed.upsert.upsert
import no.iktdev.streamitapi.classes.Profile
import no.iktdev.streamitapi.database.profiles
import org.jetbrains.exposed.sql.*

import org.jetbrains.exposed.sql.statements.InsertStatement
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.stereotype.Service

@Service
class ProfileService
{

    fun upsertProfile(profile: Profile)
    {
        transaction {
            val result = profiles
                .select { profiles.guid eq profile.guid }
                .singleOrNull()
            run {
                if (result != null)
                {
                    profiles.update({ profiles.guid eq profile.guid })
                    {
                        it[profiles.username] = profile.username
                        it[profiles.image] = profile.image
                    }
                }
                else
                {
                    profiles.insert {
                        it[guid] = profile.guid
                        it[profiles.username] = profile.username
                        it[profiles.image] = profile.image
                    }
                }
            }

            /*profiles.upsert(conflictColumn = profiles.guid,
            insertBody = {
                profile.image
                profile.username
                profile.image
            },
            updateBody = {
                profile.username
                profile.image
            })*/
        }
    }

    fun deleteProfile(guid: String)
    {
        transaction {
            profiles
                .deleteWhere { profiles.guid eq guid }
        }
    }

}