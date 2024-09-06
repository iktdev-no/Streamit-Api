package no.iktdev.streamit.api.controllers

import no.iktdev.streamit.api.classes.User
import no.iktdev.streamit.api.controllers.annotations.Authentication
import no.iktdev.streamit.api.controllers.annotations.AuthenticationModes
import no.iktdev.streamit.api.database.queries.QUser
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

open class UserController {

    @GetMapping("/user")
    open fun allUsers(): List<User> {
        return QUser().selectAll()
    }

    @GetMapping("/user/{guid}")
    open fun getUserByGuid(@PathVariable guid: String): User? {
        return QUser().selectWidth(guid)
    }


    /**
     * Post Mapping below
     **/

    @PostMapping("/user")
    open fun createOrUpdateUser(@RequestBody user: User): ResponseEntity<String> {
        QUser().upsert(user)
        return ResponseEntity("User Updated or Created", HttpStatus.OK)
    }

    @DeleteMapping("/user")
    open fun deleteUser(@RequestBody user: User): ResponseEntity<String>
    {
        val succeeded = QUser().deleteWith(user.guid)
        return if (succeeded)
            ResponseEntity("Deleted user ${user.name} with Guid ${user.guid}", HttpStatus.OK)
        else
            ResponseEntity("Could not find user ${user.name} with Guid ${user.guid} to be deleted", HttpStatus.NOT_FOUND)
    }

    @RestController
    @RequestMapping(path = ["/open"])
    class OpenUser: UserController()

    @RestController
    @RequestMapping(path = ["/secure"])
    class RestrictedUser: UserController() {
        @Authentication(AuthenticationModes.STRICT)
        override fun createOrUpdateUser(@RequestBody user: User): ResponseEntity<String> {
            return super.createOrUpdateUser(user)
        }

        @Authentication(AuthenticationModes.STRICT)
        override fun deleteUser(@RequestBody user: User): ResponseEntity<String> {
            return super.deleteUser(user)
        }

        @Authentication(AuthenticationModes.SOFT)
        override fun allUsers(): List<User> {
            return super.allUsers()
        }

        @Authentication(AuthenticationModes.SOFT)
        override fun getUserByGuid(guid: String): User? {
            return super.getUserByGuid(guid)
        }
    }

}