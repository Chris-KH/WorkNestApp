package com.apcs.worknestapp.di

import android.content.Context
import com.apcs.worknestapp.data.local.ThemeDataStore
import com.apcs.worknestapp.data.remote.auth.AuthRepository
import com.apcs.worknestapp.data.remote.auth.AuthRepositoryImpl
import com.apcs.worknestapp.data.remote.auth.UserSessionManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
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

    @Provides
    @Singleton
    fun provideThemeDataStore(
        @ApplicationContext context: Context,
    ): ThemeDataStore = ThemeDataStore(context)
}
