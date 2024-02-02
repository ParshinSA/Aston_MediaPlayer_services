package com.example.mediaplayer.data.repositories

import com.example.mediaplayer.R
import com.example.mediaplayer._comon.models.TrackMock
import com.example.mediaplayer.presentation.dependencies.MediaRepository

class MediaRepositoryImpl : MediaRepository {

    override fun getPlayList(): List<TrackMock> {
        return listOf(
            TrackMock(
                title = "Black Betty Edit",
                albumImage = R.drawable.image_black_betty_edit,
                playingRes = R.raw.song_black_betty_edit
            ),
            TrackMock(
                title = "I can Fly",
                albumImage = R.drawable.image_i_can_fly,
                playingRes = R.raw.song_i_can_fly
            ),
            TrackMock(
                title = "It serves you right",
                albumImage = R.drawable.image_it_serves_you_right_to_suffer,
                playingRes = R.raw.song_it_serves_you_right_to_suffer
            ),

            )
    }

}