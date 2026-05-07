package com.idat.presentation.configuracion

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.idat.data.local.preferences.UserPreferencesManager
import com.idat.domain.model.Usuario
import com.idat.domain.repository.UsuarioRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import android.content.Context

@HiltViewModel
class ConfiguracionViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val preferencesManager: UserPreferencesManager,
    private val usuarioRepository: UsuarioRepository,
    @param:ApplicationContext private val context: Context
) : ViewModel() {

    private val storage = FirebaseStorage.getInstance()

    private val _usuario = MutableStateFlow<Usuario?>(null)
    val usuario = _usuario.asStateFlow()

    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage

    private val _isDarkTheme = MutableStateFlow(false)
    val isDarkTheme: StateFlow<Boolean> = _isDarkTheme

    private val _isUploading = MutableStateFlow(false)
    val isUploading: StateFlow<Boolean> = _isUploading

    init {
        auth.currentUser?.let { user ->
            _email.value = user.email ?: ""
            cargarDatosUsuario(user.uid)
        }
        
        auth.addAuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            _email.value = user?.email ?: ""
            user?.let { cargarDatosUsuario(it.uid) }
        }

        viewModelScope.launch {
            preferencesManager.isDarkTheme.collect { isDark ->
                _isDarkTheme.value = isDark
            }
        }
    }

    private fun cargarDatosUsuario(uid: String) {
        viewModelScope.launch {
            usuarioRepository.getUsuario(uid).collect { user ->
                _usuario.value = user
            }
        }
    }

    fun subirFotoPerfil(uri: Uri) {
        val uid = auth.currentUser?.uid ?: return
        _isUploading.value = true
        
        val storageRef = storage.reference.child("perfiles/$uid.jpg")
        
        viewModelScope.launch {
            try {
                storageRef.putFile(uri).await()
                val downloadUrl = storageRef.downloadUrl.await().toString()
                
                val currentUsuario = _usuario.value
                if (currentUsuario != null) {
                    val usuarioActualizado = currentUsuario.copy(fotoUrl = downloadUrl)
                    usuarioRepository.guardarUsuario(usuarioActualizado)
                    _successMessage.value = "Foto de perfil actualizada"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error al subir imagen: ${e.localizedMessage}"
            } finally {
                _isUploading.value = false
            }
        }
    }

    fun guardarPerfil(
        nombre: String,
        apellido: String,
        dni: String,
        telefono: String
    ) {
        viewModelScope.launch {
            val uid = auth.currentUser?.uid ?: return@launch
            val email = auth.currentUser?.email ?: ""
            val fotoUrlActual = _usuario.value?.fotoUrl ?: ""
            val direccionActual = _usuario.value?.direccion ?: ""
            val distritoActual = _usuario.value?.distrito ?: ""
            val departamentoActual = _usuario.value?.departamento ?: ""
            val latitudActual = _usuario.value?.latitud
            val longitudActual = _usuario.value?.longitud
            
            val nuevoUsuario = Usuario(
                uid = uid,
                nombre = nombre,
                apellido = apellido,
                email = email,
                dni = dni,
                telefono = telefono,
                direccion = direccionActual,
                distrito = distritoActual,
                departamento = departamentoActual,
                fotoUrl = fotoUrlActual,
                latitud = latitudActual,
                longitud = longitudActual
            )
            
            usuarioRepository.guardarUsuario(nuevoUsuario).fold(
                onSuccess = {
                    _successMessage.value = "Perfil actualizado correctamente"
                },
                onFailure = {
                    _errorMessage.value = "Error al guardar perfil: ${it.message}"
                }
            )
        }
    }

    fun cambiarPassword(currentPassword: String, newPassword: String) {
        viewModelScope.launch {
            val user = auth.currentUser
            if (user != null && user.email != null) {
                val credential = EmailAuthProvider.getCredential(user.email!!, currentPassword)
                
                user.reauthenticate(credential)
                    .addOnCompleteListener { reauthTask ->
                        if (reauthTask.isSuccessful) {
                            user.updatePassword(newPassword)
                                .addOnCompleteListener { updateTask ->
                                    if (updateTask.isSuccessful) {
                                        _successMessage.value = "Contraseña cambiada correctamente"
                                    } else {
                                        _errorMessage.value = updateTask.exception?.message ?: "Error al cambiar contraseña"
                                    }
                                }
                        } else {
                            _errorMessage.value = "Contraseña actual incorrecta"
                        }
                    }
            }
        }
    }

    fun clearMessages() {
        _errorMessage.value = null
        _successMessage.value = null
    }
}
