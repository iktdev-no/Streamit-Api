package no.iktdev.streamitapi.controllers

import no.iktdev.streamitapi.classes.*
import no.iktdev.streamitapi.database.*
import no.iktdev.streamitapi.services.ProfileService
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.web.bind.annotation.*

@RestController
class ProfileController
{
    @GetMapping("/profile")
    fun profiles(): List<Profile>
    {
        val _profiles: MutableList<Profile> = mutableListOf()
        transaction(DataSource().getConnection())
        {
            profiles
                .selectAll()
                .map {
                    _profiles.add(Profile.fromRow(it))
                }
        }
        return _profiles
    }

    @GetMapping("/profile/{guid}")
    fun profile(@PathVariable guid: String): Profile?
    {
        var _profile: Profile? = null
        transaction(DataSource().getConnection())
        {
            _profile = Profile.fromRow(
                profiles
                    .select { profiles.guid eq guid }
                    .single()
            )
        }
        return _profile
    }


    /*
    * Post Mapping below
    * */

    @PostMapping("/profile")
    fun createProfile(@RequestBody profile: Profile): Response
    {
        ProfileService().upsertProfile(profile)
        return Response()
    }

    @DeleteMapping("/profile")
    fun deleteProfile(@RequestBody profile: Profile): Response
    {
        ProfileService().deleteProfile(profile.guid)
        return Response()
    }

}