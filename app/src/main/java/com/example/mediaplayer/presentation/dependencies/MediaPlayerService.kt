package com.example.mediaplayer.presentation.dependencies

import com.example.mediaplayer._comon.models.TrackMock
import com.example.mediaplayer.presentation.PlayingTrackInfo
import kotlinx.coroutines.flow.StateFlow

interface MediaPlayerService {
    fun playingTrackInfo(): StateFlow<PlayingTrackInfo?>
    fun isPlaying(): StateFlow<Boolean>
    fun setPlayList(list: List<TrackMock>)
    fun pauseTrack()
    fun backTrack()
    fun nextTrack()
    fun playTrack()
}