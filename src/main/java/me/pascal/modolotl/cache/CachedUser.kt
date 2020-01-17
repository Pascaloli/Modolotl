package me.pascal.modolotl.cache

data class CachedUser(var userId: String, var roles: List<String>, var mutedAt: Long? = null, var mutedUntil: Long? = null, var mutedBy: String? = null, var bannedBy: String? = null) {
    val isBanned = bannedBy != null
    val isMuted = mutedAt != null && mutedBy != null && mutedBy != null
}