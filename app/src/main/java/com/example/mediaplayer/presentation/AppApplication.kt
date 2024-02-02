package com.example.mediaplayer.presentation

import android.app.Application
import com.example.mediaplayer._comon.dependency_inject.AppComponent
import com.example.mediaplayer._comon.dependency_inject.DaggerAppComponent
import com.example.mediaplayer._comon.notifications.NotificationChannelStore
import javax.inject.Inject

class AppApplication : Application() {

    val component: AppComponent by lazy {
        DaggerAppComponent.builder()
            .addContext(context = this)
            .build()
    }

    @Inject
    lateinit var notificationChannelStore: NotificationChannelStore

    override fun onCreate() {
        component.inject(this)
        notificationChannelStore.createChannels()
        super.onCreate()
    }
}