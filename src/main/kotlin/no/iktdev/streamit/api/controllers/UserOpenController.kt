package no.iktdev.streamit.api.controllers

import no.iktdev.streamit.api.classes.Response
import no.iktdev.streamit.api.classes.User
import no.iktdev.streamit.api.controllers.logic.UserLogic
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping(path = ["/open"])
class UserOpenController {

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
    fun createOrUpdateUser(@RequestBody user: User): ResponseEntity<String> {
        UserLogic.Post().updateOrInsertUser(user)
        return ResponseEntity("User Updated or Created", HttpStatus.OK)
    }

    @DeleteMapping("/user")
    fun deleteUser(@RequestBody user: User): ResponseEntity<String>
    {
        val succeeded = UserLogic.Delete().deleteUserByGuid(user.guid)
        return if (succeeded)
            ResponseEntity("Deleted user ${user.name} with Guid ${user.guid}", HttpStatus.OK)
        else
            ResponseEntity("Could not find user ${user.name} with Guid ${user.guid} to be deleted", HttpStatus.NOT_FOUND)
    }

}