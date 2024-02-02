package com.example.mediaplayer._comon.dependency_inject.modules

import android.content.Context
import com.example.mediaplayer._comon.notifications.NotificationChannelStore
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class AppModule {

    @Provides
    @Singleton
    fun providesNotificationChannelStore(context: Context): NotificationChannelStore {
        return NotificationChannelStore(context)
    }
}