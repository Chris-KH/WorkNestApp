package com.apcs.worknestapp.di

import android.content.Context
import com.apcs.worknestapp.data.local.language.LanguageDataStore
import com.apcs.worknestapp.data.local.theme.ThemeDataStore
import com.apcs.worknestapp.data.remote.auth.AuthRepository
import com.apcs.worknestapp.data.remote.auth.AuthRepositoryImpl
import com.apcs.worknestapp.data.remote.auth.GoogleAuthUiClient
import com.apcs.worknestapp.data.remote.auth.SessionManager
import com.apcs.worknestapp.data.remote.message.MessageRepository
import com.apcs.worknestapp.data.remote.message.MessageRepositoryImpl
import com.apcs.worknestapp.data.remote.note.NoteRepository
import com.apcs.worknestapp.data.remote.note.NoteRepositoryImpl
import com.apcs.worknestapp.data.remote.notification.NotificationRepository
import com.apcs.worknestapp.data.remote.notification.NotificationRepositoryImpl
import com.apcs.worknestapp.data.remote.user.UserRepository
import com.apcs.worknestapp.data.remote.user.UserRepositoryImpl
import com.apcs.worknestapp.data.remote.board.BoardRepository
import com.apcs.worknestapp.data.remote.board.BoardRepositoryImpl
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
    fun provideAuthRepository(
        googleAuthUiClient: GoogleAuthUiClient,
        sessionManager: SessionManager,
    ): AuthRepository = AuthRepositoryImpl(googleAuthUiClient, sessionManager)

    @Provides
    @Singleton
    fun provideUserSessionManager(
        userRepo: UserRepository,
        noteRepo: NoteRepository,
        notificationRepo: NotificationRepository,
        messageRepo: MessageRepository,
    ): SessionManager = SessionManager(
        userRepo = userRepo,
        noteRepo = noteRepo,
        notificationRepo = notificationRepo,
        messageRepo = messageRepo,
    )

    @Provides
    @Singleton
    fun provideThemeDataStore(
        @ApplicationContext context: Context,
    ): ThemeDataStore = ThemeDataStore(context)

    @Provides
    @Singleton
    fun provideLanguageDataStore(
        @ApplicationContext context: Context,
    ): LanguageDataStore = LanguageDataStore(context)

    @Provides
    @Singleton
    fun provideNotificationRepository(): NotificationRepository = NotificationRepositoryImpl()

    @Provides
    @Singleton
    fun provideNoteRepository(): NoteRepository = NoteRepositoryImpl()

    @Provides
    @Singleton
    fun provideUserRepository(): UserRepository = UserRepositoryImpl()

    @Provides
    @Singleton
    fun provideMessageRepository(): MessageRepository = MessageRepositoryImpl()
    @Provides
    @Singleton
    fun provideBoardRepository(): BoardRepository = BoardRepositoryImpl()
}
