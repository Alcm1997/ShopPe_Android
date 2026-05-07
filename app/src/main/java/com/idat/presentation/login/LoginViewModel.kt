package com.idat.presentation.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.idat.data.local.preferences.UserPreferencesManager
import com.idat.domain.model.Usuario
import com.idat.domain.repository.UsuarioRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val usuarioRepository: UsuarioRepository,
    private val preferencesManager: UserPreferencesManager
) : ViewModel() {

    private val _loginExitoso = MutableStateFlow(false)
    val loginExitoso: StateFlow<Boolean> = _loginExitoso

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage

    private val _nombreUsuario = MutableStateFlow("Usuario")
    val nombreUsuario: StateFlow<String> = _nombreUsuario

    private val _rememberMe = MutableStateFlow(false)
    val rememberMe: StateFlow<Boolean> = _rememberMe

    private val _savedEmail = MutableStateFlow("")
    val savedEmail: StateFlow<String> = _savedEmail

    private val _savedPassword = MutableStateFlow("")
    val savedPassword: StateFlow<String> = _savedPassword

    init {
        viewModelScope.launch {
            _rememberMe.value = preferencesManager.rememberMe.firstOrNull() ?: false
            if (_rememberMe.value) {
                _savedEmail.value = preferencesManager.savedEmail.firstOrNull() ?: ""
                _savedPassword.value = preferencesManager.savedPassword.firstOrNull() ?: ""
            }
        }
    }

    fun setRememberMe(enabled: Boolean) {
        _rememberMe.value = enabled
    }

    fun iniciarSesion(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _errorMessage.value = "Por favor, ingresa tu correo y contraseña."
            return
        }

        viewModelScope.launch {
            _errorMessage.value = null
            _successMessage.value = null
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val firebaseUser = auth.currentUser
                        if (firebaseUser != null) {
                            viewModelScope.launch {
                                // Buscamos el nombre real en Firestore
                                val usuarioFirestore = usuarioRepository.getUsuario(firebaseUser.uid).firstOrNull()
                                _nombreUsuario.value = usuarioFirestore?.nombre ?: "Usuario"
                                
                                preferencesManager.setRememberMe(_rememberMe.value, email, password)
                                _loginExitoso.value = true
                            }
                        }
                    } else {
                        val error = when (task.exception) {
                            is FirebaseAuthInvalidUserException -> "El correo electrónico no se encuentra registrado."
                            is FirebaseAuthInvalidCredentialsException -> "La contraseña es incorrecta."
                            else -> "Ocurrió un error inesperado."
                        }
                        _errorMessage.value = error
                    }
                }
        }
    }

    fun enviarCorreoRecuperacion(email: String) {
        if (email.isBlank()) {
            _errorMessage.value = "Ingresa tu correo para enviarte el enlace."
            return
        }
        
        viewModelScope.launch {
            _errorMessage.value = null
            _successMessage.value = null
            auth.sendPasswordResetEmail(email)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        _successMessage.value = "Te hemos enviado un enlace de recuperación a tu correo."
                    } else {
                        _errorMessage.value = "No se pudo enviar el correo. Verifica que sea el correcto."
                    }
                }
        }
    }

    fun limpiarMensajes() {
        _errorMessage.value = null
        _successMessage.value = null
    }

    fun loginWithGoogleCredential(result: SignInResult) {
        viewModelScope.launch {
            _errorMessage.value = null
            result.credential?.let { credential ->
                auth.signInWithCredential(credential)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val firebaseUser = auth.currentUser
                            if (firebaseUser != null) {
                                viewModelScope.launch {
                                    val usuarioExistente = usuarioRepository.getUsuario(firebaseUser.uid).firstOrNull()
                                    val googlePhotoUrl = firebaseUser.photoUrl?.toString() ?: ""
                                    
                                    if (usuarioExistente == null) {
                                        val nombresCompletos = firebaseUser.displayName?.split(" ") ?: listOf("", "")
                                        val nombre = nombresCompletos.getOrNull(0) ?: ""
                                        val apellido = if (nombresCompletos.size > 1) nombresCompletos.drop(1).joinToString(" ") else ""
                                        
                                        _nombreUsuario.value = nombre
                                        val nuevoUsuario = Usuario(
                                            uid = firebaseUser.uid,
                                            nombre = nombre,
                                            apellido = apellido,
                                            email = firebaseUser.email ?: "",
                                            fotoUrl = googlePhotoUrl
                                        )
                                        usuarioRepository.guardarUsuario(nuevoUsuario)
                                    } else {
                                        _nombreUsuario.value = usuarioExistente.nombre
                                        if (usuarioExistente.fotoUrl.isEmpty() && googlePhotoUrl.isNotEmpty()) {
                                            val usuarioConFoto = usuarioExistente.copy(fotoUrl = googlePhotoUrl)
                                            usuarioRepository.guardarUsuario(usuarioConFoto)
                                        }
                                    }
                                    _loginExitoso.value = true
                                }
                            }
                        } else {
                            _errorMessage.value = "Error al autenticar con Google."
                        }
                    }
            } ?: run {
                _errorMessage.value = result.errorMessage ?: "No se obtuvieron credenciales."
            }
        }
    }
}
