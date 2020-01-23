package me.pascal.modolotl.cache

import me.pascal.modolotl.Modolotl
import me.pascal.modolotl.utils.Logger
import me.pascal.modolotl.utils.UnmuteTasks
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.User
import java.util.*
import kotlin.collections.ArrayList

class CachingHandler {

    private val cache: ArrayList<CachedUser> = arrayListOf()

    fun init() {
        try {
            Logger.log("Getting Users from Database")
            val usersInDb = Modolotl.dbConnection.createStatement().use { statement ->
                val rs = statement.executeQuery("SELECT * FROM users")
                generateSequence {
                    if (rs.next())
                        CachedUser(rs.getString(1), //UserID
                                rs.getString(2).split(","), //Roles
                                rs.getLong(3), //MutedAt
                                rs.getLong(4), //MutedUntil
                                rs.getString(5), //MutedBy
                                rs.getString(6) //BannedBy
                        ) else null
                }.toList()
            }

            Logger.debug("Loaded ${usersInDb.size} Users from Database")

            //Adding Users to Cache List
            cache.addAll(usersInDb)

            cache.forEach {
                if (it.isMuted() && it.mutedUntil != null) {
                    val member = Modolotl.jda.guilds[0].getMemberById(it.userId)!!
                    if (System.currentTimeMillis() > it.mutedUntil!!) {
                        Modolotl.jda.guilds[0].removeRoleFromMember(member, Modolotl.mutedRole).queue()
                    } else {
                        val timer = Timer()
                        val task = object : TimerTask() {
                            override fun run() {
                                if (member.roles.contains(Modolotl.mutedRole)) {
                                    member.guild.removeRoleFromMember(member, Modolotl.mutedRole).queue()
                                }
                            }
                        }
                        timer.schedule(task, it.mutedUntil!! - it.mutedAt!!)
                        UnmuteTasks.addTask(it.userId, task)
                    }
                }
            }

            //Adding missing users into DB/cache
            val start = System.currentTimeMillis()
            var counter = 0
            Logger.log("Inserting missing Users into Database")
            Modolotl.dbConnection.createStatement().use {
                Modolotl.jda.guilds.forEach { guild ->
                    guild.members.forEach { member ->
                        if (getUserById(member.user.id) == null) {
                            addUser(member)
                            counter++
                        }
                    }
                }
            }
            val end = System.currentTimeMillis()
            if (counter > 0) {
                Logger.log("Inserted $counter Users in ${end - start}ms")
            } else {
                Logger.log("Database is already up to date")
            }
        } catch (e: Exception) {
            Logger.error("Couldn't initialise User Cache")
            e.printStackTrace()
        }
    }

    fun addUser(member: Member, force: Boolean) {
        val userId = member.user.id
        if (force || getUserById(userId) == null) {
            val roles = member.roles.joinToString(",") { it.id }
            Modolotl.dbConnection.createStatement().use {
                it.executeUpdate("INSERT INTO users(userId, roles) VALUES ($userId, '$roles')")
                cache.add(CachedUser(userId, roles.split(",")))
            }
            Logger.debug("Inserted new User ${member.effectiveName}")
        } else {
            Logger.debug("Tried to add an existing user to the Database")
            return
        }
    }

    fun addUser(member: Member) {
        addUser(member, false)
    }

    fun getUserById(id: String): CachedUser? {
        return cache.find { it.userId == id }
    }

    fun updateRoles(member: Member) {
        val cachedUser = getUserById(member.id)!!
        val roles = member.roles.joinToString(",") { it.id }
        Modolotl.dbConnection.prepareStatement("UPDATE users SET roles = ? WHERE userId = ?").use {
            it.setString(1, roles)
            it.setString(2, member.id)
            cachedUser.roles = roles.split(",")
            it.execute()
        }
    }

    fun updateBan(toBan: User, banner: Member, cachedUser: CachedUser) {
        if (cachedUser.isBanned()) {
            //Unban User
            Modolotl.dbConnection.createStatement().use {
                it.executeUpdate("UPDATE users SET bannedBy = null WHERE userId='${toBan.id}'")
            }
            cachedUser.bannedBy = null
        } else {
            //Ban User
            Modolotl.dbConnection.createStatement().use {
                it.executeUpdate("UPDATE users SET bannedBy = '${banner.id}' WHERE userId='${toBan.id}'")
            }
            cachedUser.bannedBy = banner.id
        }
    }

    fun updateMute(muted: Member, muter: User?, mutedAt: Long?, mutedUntil: Long?, cachedUser: CachedUser) {
        if (!cachedUser.isMuted()) {
            //muten
            if (mutedUntil != null && mutedAt != null) {
                val timer = Timer()
                val task = object : TimerTask() {
                    override fun run() {
                        if (muted.roles.contains(Modolotl.mutedRole)) {
                            muted.guild.removeRoleFromMember(muted, Modolotl.mutedRole).queue()
                        }
                    }
                }
                timer.schedule(task, mutedUntil - mutedAt)
                println("Timer scheduled: ${mutedUntil - mutedAt}")
                UnmuteTasks.addTask(muted.id, task)
            }
            Modolotl.dbConnection.createStatement().use {
                it.executeUpdate("UPDATE users SET mutedAt=$mutedAt, mutedUntil=$mutedUntil, mutedBy='${muter!!.id}' WHERE userId='${muted.id}'")
            }
            cachedUser.mutedAt = mutedAt
            cachedUser.mutedUntil = mutedUntil
            cachedUser.mutedBy = muter!!.id

        } else {
            //unmuten
            Modolotl.dbConnection.createStatement().use {
                it.executeUpdate("UPDATE users SET mutedAt= null, mutedUntil= null, mutedBy=null WHERE userId='${muted.id}'")
            }
            cachedUser.mutedAt = null
            cachedUser.mutedUntil = null
            cachedUser.mutedBy = null
        }

    }
}