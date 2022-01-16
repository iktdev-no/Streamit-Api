package no.iktdev.streamitapi.controllers

import no.iktdev.streamitapi.classes.*
import no.iktdev.streamitapi.database.*
import no.iktdev.streamitapi.services.database.UserService
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.web.bind.annotation.*

@RestController
class UserController
{
    @GetMapping("/user")
    fun users(): List<User>
    {
        val _users: MutableList<User> = mutableListOf()
        transaction(DataSource().getConnection())
        {
            users
                .selectAll()
                .map {
                    _users.add(User.fromRow(it))
                }
        }
        return _users
    }

    @GetMapping("/user/{guid}")
    fun profile(@PathVariable guid: String): User?
    {
        var _user: User? = null
        transaction(DataSource().getConnection())
        {
            _user = User.fromRow(
                users
                    .select { users.guid eq guid }
                    .single()
            )
        }
        return _user
    }


    /*
    * Post Mapping below
    * */

    @PostMapping("/user")
    fun createProfile(@RequestBody user: User): Response
    {
        UserService().upsertUser(user)
        return Response()
    }

    @DeleteMapping("/profile")
    fun deleteProfile(@RequestBody user: User): Response
    {
        UserService().deleteUser(user.guid)
        return Response()
    }

}