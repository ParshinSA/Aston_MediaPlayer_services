package com.example.mediaplayer.presentation.dependencies

import com.example.mediaplayer._comon.models.TrackMock

interface MediaRepository {
    fun getPlayList(): List<TrackMock>
}