package com.example.mediaplayer.presentation

import android.Manifest.permission.POST_NOTIFICATIONS
import android.app.Dialog
import android.app.Service
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.mediaplayer.R
import com.example.mediaplayer._comon.services.MediaPlayerServiceImpl
import com.example.mediaplayer.databinding.ActivityMainBinding
import com.example.mediaplayer.presentation.dependencies.MediaRepository
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var mediaRepository: MediaRepository

    private var mediaPlayerService: MediaPlayerServiceImpl? = null
    private var shouldUnbindPlayerService = false
    private var dialog: Dialog? = null
    private var isPlaying = false

    private val mediaPlayerIntent by lazy { Intent(this, MediaPlayerServiceImpl::class.java) }
    private val timeTrackerFormat = SimpleDateFormat("mm:ss", Locale.getDefault())

    private val connection by lazy {
        object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName?, service: IBinder) {
                mediaPlayerService = (service as MediaPlayerServiceImpl.LocalBinder).getService()
                mediaPlayerService?.setPlayList(mediaRepository.getPlayList())
                observeMediaPlayerData()
            }

            override fun onServiceDisconnected(name: ComponentName?) {}
        }
    }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {}

    private val binding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    ////////////////////////////// Lifecycle methods
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        inject()
        thisSettings()
    }

    override fun onDestroy() {
        if (!isPlaying) {
            mediaPlayerService?.stopForeground(Service.STOP_FOREGROUND_REMOVE)
            mediaPlayerService?.stopSelf()
        }
        mediaPlayerService = null
        dialog?.dismiss()
        dialog = null
        super.onDestroy()
    }
    ////////////////////////////// Lifecycle methods

    private fun thisSettings() {
        checkNotificationPermissions()
        startMediaService()
        bindMediaService()
        bindPlayerController()
    }

    private fun checkNotificationPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            when {
                shouldShowRequestPermissionRationale(POST_NOTIFICATIONS) -> {
                    dialog?.dismiss()
                    dialog = AlertDialog.Builder(this)
                        .setMessage(getString(R.string.rationale_media_notofication))
                        .setNegativeButton("No") { dialog, _ ->
                            dialog.dismiss()
                        }
                        .setPositiveButton("Yes") { dialog, _ ->
                            dialog.dismiss()
                            requestPermissionLauncher.launch(POST_NOTIFICATIONS)
                        }.show()
                }

                checkSelfPermission(POST_NOTIFICATIONS) != PERMISSION_GRANTED -> {
                    requestPermissionLauncher.launch(POST_NOTIFICATIONS)
                }
            }
    }

    private fun bindPlayerController() {
        binding.btnPlayPause.setOnClickListener {
            if (!isPlaying) mediaPlayerService?.playTrack() else mediaPlayerService?.pauseTrack()
        }

        binding.btnBack.setOnClickListener {
            mediaPlayerService?.backTrack()
        }

        binding.btnNext.setOnClickListener {
            mediaPlayerService?.nextTrack()
        }
    }

    private fun observeMediaPlayerData() {
        lifecycleScope.launch {
            mediaPlayerService?.isPlaying()?.onEach { handleChangePlaying(it) }?.collect()
        }
        lifecycleScope.launch {
            mediaPlayerService?.playingTrackInfo()?.onEach { handleTrackInfo(it) }?.collect()
        }
    }

    private fun handleTrackInfo(trackInfo: PlayingTrackInfo?) {
        if (trackInfo == null) return
        updateAlbumImage(trackInfo)
        updateTitle(trackInfo.track.title)
        updateCurrentPositionTrack(trackInfo.currentPosition)
        updateDuration(trackInfo.duration)
        updateLineTimeTracker(trackInfo)
    }

    private fun updateAlbumImage(trackInfo: PlayingTrackInfo) {
        if (binding.trackTitle.text == trackInfo.track.title) return

        Glide.with(this)
            .load(trackInfo.track.albumImage)
            .error(R.drawable.ic_logo_mediaplayer)
            .into(binding.albumImage)
    }

    private fun updateLineTimeTracker(trackInfo: PlayingTrackInfo) {
        binding.lineTimeTracker.max = trackInfo.duration
        binding.lineTimeTracker.progress = trackInfo.currentPosition
    }

    private fun updateDuration(durationMillis: Int) {
        val date = Date(durationMillis.toLong())
        binding.durationTrack.text = timeTrackerFormat.format(date)
    }

    private fun updateTitle(title: String) {
        binding.trackTitle.text = title
    }

    private fun updateCurrentPositionTrack(currentPositionMillis: Int) {
        val date = Date(currentPositionMillis.toLong()).time
        binding.currentPositionTrack.text = timeTrackerFormat.format(date)
    }

    private fun handleChangePlaying(isPlaying: Boolean) {
        this.isPlaying = isPlaying
        binding.btnPlayPause.setImageDrawable(
            ResourcesCompat.getDrawable(
                resources,
                if (isPlaying) R.drawable.ic_pause_track else R.drawable.ic_play_track,
                null
            )
        )

        binding.btnNext.isEnabled = isPlaying
        binding.btnBack.isEnabled = isPlaying
    }

    private fun startMediaService() {
        startService(mediaPlayerIntent)
    }

    private fun bindMediaService() {
        if (bindService(mediaPlayerIntent, connection, BIND_AUTO_CREATE)
        ) shouldUnbindPlayerService = true
        else {
            val errorMessage = getString(R.string.failedToStartedMediaPlayer)
            Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
        }
    }

    private fun inject() {
        (applicationContext as AppApplication).component.inject(this)
    }
}