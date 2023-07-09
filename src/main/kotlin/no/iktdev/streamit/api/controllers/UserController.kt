package no.iktdev.streamit.api.controllers

import no.iktdev.streamit.api.classes.User
import no.iktdev.streamit.api.controllers.annotations.Authentication
import no.iktdev.streamit.api.controllers.annotations.AuthenticationModes
import no.iktdev.streamit.api.controllers.logic.UserLogic
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

open class UserController {

    @GetMapping("/user")
    fun allUsers(): List<User> {
        return UserLogic.Get().allUsers()
    }

    @GetMapping("/user/{guid}")
    fun getUserByGuid(@PathVariable guid: String): User? {
        return UserLogic.Get().getUserByGuid(guid)
    }


    /**
     * Post Mapping below
     **/

    @PostMapping("/user")
    open fun createOrUpdateUser(@RequestBody user: User): ResponseEntity<String> {
        UserLogic.Post().updateOrInsertUser(user)
        return ResponseEntity("User Updated or Created", HttpStatus.OK)
    }

    @DeleteMapping("/user")
    open fun deleteUser(@RequestBody user: User): ResponseEntity<String>
    {
        val succeeded = UserLogic.Delete().deleteUserByGuid(user.guid)
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
    }

}