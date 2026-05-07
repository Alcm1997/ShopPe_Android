package com.idat.presentation.login

import com.google.firebase.auth.AuthCredential


data class SignInResult(
    val credential: AuthCredential? = null,
    val errorMessage: String? = null
)
