package com.example.mediaplayer._comon.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationManagerCompat
import com.example.mediaplayer.R

class NotificationChannelStore(
    private val context: Context
) {

    fun createChannels() {
        createMediaPlayerNotificationChannel()
    }

    private fun createMediaPlayerNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val ntfName = context.getString(R.string.mediaPlayerNtfName)
            val ntfPriority = NotificationManager.IMPORTANCE_LOW
            val ntfDescription = context.getString(R.string.mediaPlayerNtfDescription)

            val channel = NotificationChannel(MEDIA_PLAYER_NTF_CHANNEL_ID, ntfName, ntfPriority)
                .apply { description = ntfDescription }

            NotificationManagerCompat.from(context).createNotificationChannel(channel)
        }
    }

    companion object {
        const val MEDIA_PLAYER_NTF_CHANNEL_ID = "MEDIA_PLAYER_NTF_CHANNEL_ID"
    }
}
