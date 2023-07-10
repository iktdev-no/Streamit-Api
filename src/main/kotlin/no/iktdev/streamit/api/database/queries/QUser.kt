package no.iktdev.streamit.api.database.queries

import no.iktdev.streamit.api.classes.User
import no.iktdev.streamit.library.db.tables.users
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

class QUser {

    fun selectAll(): List<User> {
        return transaction {
            users.selectAll().mapNotNull { User.fromRow(it) }
        }
    }
    fun selectWidth(id: String): User? {
        return transaction {
            users.selectAll()
                .andWhere { users.guid eq id }
                .mapNotNull { User.fromRow(it) }.firstOrNull()
        }
    }

    fun upsert(user: User) {
        val present = selectWidth(user.guid)
        transaction {
            if (present != null) {
                users.update({users.guid eq user.guid}) {
                    it[name] = user.name
                    it[image] = user.image
                }
            } else {
                users.insert {
                    it[guid] = user.guid
                    it[name] = user.name
                    it[image] = user.image
                }
            }
        }
    }

    fun deleteWith(id: String) {
        transaction {
            users.deleteWhere { users.guid eq id }
        }
    }





}