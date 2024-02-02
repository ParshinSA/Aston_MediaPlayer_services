package com.example.mediaplayer._comon.services

import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.os.Binder
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.mediaplayer.R
import com.example.mediaplayer._comon.models.TrackMock
import com.example.mediaplayer._comon.notifications.NotificationChannelStore.Companion.MEDIA_PLAYER_NTF_CHANNEL_ID
import com.example.mediaplayer._comon.services.MediaPlayerServiceImpl.MediaPlayerPendingActions.CLOSE_PLAYER
import com.example.mediaplayer._comon.services.MediaPlayerServiceImpl.MediaPlayerPendingActions.NEXT_TRACK
import com.example.mediaplayer._comon.services.MediaPlayerServiceImpl.MediaPlayerPendingActions.PLAY_PAUSE
import com.example.mediaplayer.presentation.PlayingTrackInfo
import com.example.mediaplayer.presentation.dependencies.MediaPlayerService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class MediaPlayerServiceImpl : Service(), MediaPlayerService {

    private var trackInfoMutStateFlow = MutableStateFlow<PlayingTrackInfo?>(null)
    private var isPlayingMutStateFlow = MutableStateFlow(false)
    private var playList = emptyList<TrackMock>()
    private var mediaPlayer: MediaPlayer? = null
    private var currentTrack = 0

    private val notification
        get() = NotificationCompat.Builder(this, MEDIA_PLAYER_NTF_CHANNEL_ID)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setContentTitle(playList[currentTrack].title)
            .setSmallIcon(R.drawable.ic_play_track)
            .addAction(
                if (isPlayingMutStateFlow.value) R.drawable.ic_pause_track else R.drawable.ic_play_track,
                getString(if (isPlayingMutStateFlow.value) R.string.pause else R.string.play),
                playPauseTrackPendingIntent()
            )
            .addAction(
                R.drawable.ic_next_track,
                getString(R.string.nextTrack),
                nexTrackPendingIntent()
            )
            .addAction(
                R.drawable.ic_close_player,
                getString(R.string.closePlayer),
                closePlayerPendingIntent()
            )
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

    private val coroutineScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private val mediaIntent get() = Intent(this, MediaPlayerServiceImpl::class.java)

    override fun onBind(intent: Intent?): IBinder = LocalBinder()

    override fun onCreate() {
        super.onCreate()
        updateTrackInfo()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action ?: "") {
            PLAY_PAUSE.name -> if (isPlayingMutStateFlow.value) pauseTrack() else playTrack()
            NEXT_TRACK.name -> nextTrack()
            CLOSE_PLAYER.name -> {
                pauseTrack()
                this.stopForeground(STOP_FOREGROUND_REMOVE)
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        coroutineScope.cancel()
        mediaPlayer = null
        super.onDestroy()
    }

    override fun isPlaying(): StateFlow<Boolean> = isPlayingMutStateFlow.asStateFlow()

    override fun setPlayList(list: List<TrackMock>) {
        playList = list
    }

    override fun nextTrack() {
        if (isPlayingMutStateFlow.value) {
            mediaPlayer?.stop()
            currentTrack = if (currentTrack == playList.lastIndex) 0 else currentTrack + 1
            mediaPlayer = MediaPlayer.create(this, playList[currentTrack].playingRes)
            mediaPlayer?.start()
            startForeground(MEDIA_PLAYER_ID, notification)
        }
    }

    override fun backTrack() {
        if (isPlayingMutStateFlow.value) {
            mediaPlayer?.stop()
            currentTrack = if (currentTrack == 0) playList.lastIndex else currentTrack - 1
            mediaPlayer = MediaPlayer.create(this, playList[currentTrack].playingRes)
            mediaPlayer?.start()
            startForeground(MEDIA_PLAYER_ID, notification)
        }
    }

    override fun playTrack() {
        if (mediaPlayer != null && !isPlayingMutStateFlow.value) {
            mediaPlayer?.start()
        } else {
            mediaPlayer = MediaPlayer.create(this, playList[currentTrack].playingRes)
            mediaPlayer?.start()
        }

        isPlayingMutStateFlow.value = true
        startForeground(MEDIA_PLAYER_ID, notification)
    }

    override fun pauseTrack() {
        isPlayingMutStateFlow.value = false
        mediaPlayer?.pause()
        startForeground(MEDIA_PLAYER_ID, notification)
    }

    override fun playingTrackInfo() = trackInfoMutStateFlow.asStateFlow()

    private fun updateTrackInfo() {
        coroutineScope.launch {
            isPlayingMutStateFlow.onEach {
                if (playList.isEmpty()) return@onEach
                do {
                    trackInfoMutStateFlow.value = PlayingTrackInfo(
                        track = playList[currentTrack],
                        duration = mediaPlayer?.duration ?: 0,
                        currentPosition = mediaPlayer?.currentPosition ?: 0,
                    )
                    delay(500)
                } while (isPlayingMutStateFlow.value)
            }.collect()
        }
    }

    private fun playPauseTrackPendingIntent(): PendingIntent? {
        val intent = mediaIntent.apply { action = PLAY_PAUSE.name }
        return PendingIntent.getService(this, MEDIA_PLAYER_ID, intent, PendingIntent.FLAG_IMMUTABLE)
    }

    private fun closePlayerPendingIntent(): PendingIntent? {
        val intent = mediaIntent.apply { action = CLOSE_PLAYER.name }
        return PendingIntent.getService(this, MEDIA_PLAYER_ID, intent, PendingIntent.FLAG_IMMUTABLE)
    }

    private fun nexTrackPendingIntent(): PendingIntent? {
        val intent = mediaIntent.apply { action = NEXT_TRACK.name }
        return PendingIntent.getService(this, MEDIA_PLAYER_ID, intent, PendingIntent.FLAG_IMMUTABLE)
    }

    inner class LocalBinder : Binder() {
        fun getService() = this@MediaPlayerServiceImpl
    }

    private enum class MediaPlayerPendingActions { CLOSE_PLAYER, PLAY_PAUSE, NEXT_TRACK; }

    companion object {
        private const val MEDIA_PLAYER_ID = 6925
    }
}