package com.idat.presentation.registro

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.GoogleAuthProvider
import com.idat.domain.model.Usuario
import com.idat.domain.repository.UsuarioRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RegistroViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val usuarioRepository: UsuarioRepository
) : ViewModel() {

    private val _registroExitoso = MutableStateFlow(false)
    val registroExitoso: StateFlow<Boolean> = _registroExitoso

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    fun registrarUsuario(email: String, password: String, confirmarPassword: String) {
        viewModelScope.launch {
            if (email.isBlank() || password.isBlank()) {
                _errorMessage.value = "Por favor completa todos los campos"
                return@launch
            }

            if (password != confirmarPassword) {
                _errorMessage.value = "Las contraseñas no coinciden"
                return@launch
            }

            if (password.length < 6) {
                _errorMessage.value = "La contraseña debe tener al menos 6 caracteres"
                return@launch
            }

            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val firebaseUser = auth.currentUser
                        if (firebaseUser != null) {
                            val nuevoUsuario = Usuario(
                                uid = firebaseUser.uid,
                                email = email,
                                nombre = "",
                                apellido = ""
                            )
                            viewModelScope.launch {
                                usuarioRepository.guardarUsuario(nuevoUsuario)
                                _registroExitoso.value = true
                            }
                        }
                    } else {
                        val error = when (task.exception) {
                            is FirebaseAuthUserCollisionException -> "Este correo ya está registrado. Intenta iniciar sesión."
                            is FirebaseAuthInvalidCredentialsException -> "El formato del correo es inválido."
                            else -> "Ocurrió un error al registrar: ${task.exception?.localizedMessage}"
                        }
                        _errorMessage.value = error
                    }
                }
        }
    }

    fun registrarConGoogle(idToken: String) {
        viewModelScope.launch {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            auth.signInWithCredential(credential)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val firebaseUser = auth.currentUser
                        if (firebaseUser != null) {
                            val nombresCompletos = firebaseUser.displayName?.split(" ") ?: listOf("", "")
                            val nombre = nombresCompletos.getOrNull(0) ?: ""
                            val apellido = if (nombresCompletos.size > 1) nombresCompletos.drop(1).joinToString(" ") else ""
                            
                            val nuevoUsuario = Usuario(
                                uid = firebaseUser.uid,
                                email = firebaseUser.email ?: "",
                                nombre = nombre,
                                apellido = apellido,
                                fotoUrl = firebaseUser.photoUrl?.toString() ?: ""
                            )
                            viewModelScope.launch {
                                usuarioRepository.guardarUsuario(nuevoUsuario)
                                _registroExitoso.value = true
                            }
                        }
                    } else {
                        _errorMessage.value = "Error al conectar con Google. Inténtalo de nuevo."
                    }
                }
        }
    }

    fun limpiarError() {
        _errorMessage.value = null
    }
}
