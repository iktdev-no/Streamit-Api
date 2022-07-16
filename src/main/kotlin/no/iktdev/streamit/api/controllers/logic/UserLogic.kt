package no.iktdev.streamit.api.controllers.logic

import no.iktdev.streamit.api.classes.User
import no.iktdev.streamit.api.database.users
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction

class UserLogic {

    class Get {
        fun allUsers(): List<User> {
            return transaction {
                users.selectAll()
                    .mapNotNull { User.fromRow(it) }
            }
        }

        fun getUserByGuid(guid: String): User? {
            val result = transaction {
                users.select { users.guid eq guid }
                    .singleOrNull()
            }
            return if (result != null) User.fromRow(result) else null
        }
    }

    class Post {
        fun updateOrInsertUser(user: User) {
            transaction {
                val result = users.select { users.guid eq user.guid }.singleOrNull()
                if (result == null)
                    users.insert {
                        it[guid] = user.guid
                        it[users.name] = user.name
                        it[users.image] = user.image
                    }
                else
                    users.update({ users.guid eq user.guid })
                    {
                        it[users.name] = user.name
                        it[users.image] = user.image
                    }
            }
        }
    }

    class Delete {
        fun deleteUserByGuid(guid: String): Boolean {
            val result = transaction {
                users.deleteWhere { users.guid eq guid }
            }
            return result != 0
        }
    }
}