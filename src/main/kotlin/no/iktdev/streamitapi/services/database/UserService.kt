package no.iktdev.streamitapi.services.database

import no.iktdev.streamitapi.classes.User
import no.iktdev.streamitapi.database.users
import org.jetbrains.exposed.sql.*

import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.stereotype.Service

@Service
class UserService
{

    fun upsertUser(user: User)
    {
        transaction {
            val result = users
                .select { users.guid eq user.guid }
                .singleOrNull()
            run {
                if (result != null)
                {
                    users.update({ users.guid eq user.guid })
                    {
                        it[users.name] = user.name
                        it[users.image] = user.image
                    }
                }
                else
                {
                    users.insert {
                        it[guid] = user.guid
                        it[users.name] = user.name
                        it[users.image] = user.image
                    }
                }
            }

        }
    }

    fun deleteUser(guid: String)
    {
        transaction {
            users
                .deleteWhere { users.guid eq guid }
        }
    }

}