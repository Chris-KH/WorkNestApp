package com.apcs.worknestapp.di

import com.apcs.worknestapp.auth.AuthRepository
import com.apcs.worknestapp.auth.AuthRepositoryImpl
import com.apcs.worknestapp.auth.UserSessionManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun provideAuthRepository(): AuthRepository = AuthRepositoryImpl()

    @Provides
    @Singleton
    fun provideUserSessionManager(
        authRepository: AuthRepository,
    ): UserSessionManager = UserSessionManager(authRepository)
}
