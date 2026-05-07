package com.idat.di.modules

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.idat.data.repository.ProductoRepositoryImpl
import com.idat.data.repository.PedidoRepositoryImpl
import com.idat.domain.repository.ProductoRepository
import com.idat.domain.repository.PedidoRepository
import com.idat.domain.repository.UsuarioRepository
import com.idat.data.repository.UsuarioRepositoryImpl
import com.idat.data.remote.service.ProductoApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideProductoRepository(
        apiService: ProductoApiService,
        auth: FirebaseAuth,
        firestore: FirebaseFirestore
    ): ProductoRepository {
        return ProductoRepositoryImpl(apiService, auth, firestore)
    }

    @Provides
    @Singleton
    fun providePedidoRepository(
        firestore: FirebaseFirestore
    ): PedidoRepository {
        return PedidoRepositoryImpl(firestore)
    }

    @Provides
    @Singleton
    fun provideDireccionRepository(
        firestore: FirebaseFirestore
    ): com.idat.domain.repository.DireccionRepository {
        return com.idat.data.repository.DireccionRepositoryImpl(firestore)
    }

    @Provides
    @Singleton
    fun provideTarjetaRepository(
        firestore: FirebaseFirestore
    ): com.idat.domain.repository.TarjetaRepository {
        return com.idat.data.repository.TarjetaRepositoryImpl(firestore)
    }

    @Provides
    @Singleton
    fun provideUsuarioRepository(
        firestore: FirebaseFirestore
    ): UsuarioRepository {
        return UsuarioRepositoryImpl(firestore)
    }
}
