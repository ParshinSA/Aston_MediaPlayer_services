package com.example.mediaplayer._comon.dependency_inject.modules

import com.example.mediaplayer.data.repositories.MediaRepositoryImpl
import com.example.mediaplayer.presentation.dependencies.MediaRepository
import dagger.Module
import dagger.Provides

@Module
class RepositoriesModule {

    @Provides
    fun providesMediaRepository(): MediaRepository {
        return MediaRepositoryImpl()
    }

}