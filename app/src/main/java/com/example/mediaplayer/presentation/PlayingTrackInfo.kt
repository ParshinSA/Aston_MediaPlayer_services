package com.example.mediaplayer.presentation

import com.example.mediaplayer._comon.models.TrackMock

data class PlayingTrackInfo(
    val track: TrackMock,
    val duration: Int,
    val currentPosition: Int
)