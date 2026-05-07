package com.idat.di.modules

import com.idat.domain.usecase.ObtenerProductosUseCase
import com.idat.domain.usecase.CarritoUseCase
import com.idat.domain.repository.ProductoRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object UseCaseModule {

    @Provides
    fun provideObtenerProductosUseCase(
        repository: ProductoRepository
    ): ObtenerProductosUseCase {
        return ObtenerProductosUseCase(repository)
    }

    @Provides
    fun provideCarritoUseCase(
        repository: ProductoRepository
    ): CarritoUseCase {
        return CarritoUseCase(repository)
    }
}
