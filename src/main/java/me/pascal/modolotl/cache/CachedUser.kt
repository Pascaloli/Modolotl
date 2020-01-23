package me.pascal.modolotl.cache

data class CachedUser(var userId: String, var roles: List<String>, var mutedAt: Long? = null, var mutedUntil: Long? = null, var mutedBy: String? = null, var bannedBy: String? = null) {

    fun isBanned(): Boolean {
        return bannedBy != null
    }

    fun isMuted(): Boolean {
        return mutedBy != null
    }
}