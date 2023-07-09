package no.iktdev.streamit.api.controllers.logic

import no.iktdev.streamit.api.classes.User
import no.iktdev.streamit.api.database.queries.QUser
import no.iktdev.streamit.library.db.tables.users
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction

class UserLogic {

    class Get {
        fun allUsers(): List<User> {
            return QUser().selectAll()
        }

        fun getUserByGuid(guid: String): User? {
            return QUser().selectWidth(guid)
        }
    }

    class Post {
        fun updateOrInsertUser(user: User) {
            QUser().upsert(user)
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