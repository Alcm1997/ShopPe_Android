package com.idat.core.auth

object AdminAccess {
    private val adminEmails = setOf(
        "yeffercastillovega24@gmail.com"
    ).map { it.trim().lowercase() }.toSet()

    fun isAdminEmail(email: String?): Boolean {
        val normalized = email?.trim()?.lowercase() ?: return false
        return normalized in adminEmails
    }
}
