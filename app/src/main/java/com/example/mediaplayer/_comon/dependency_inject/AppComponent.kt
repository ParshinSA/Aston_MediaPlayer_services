package com.example.mediaplayer._comon.dependency_inject

import android.content.Context
import com.example.mediaplayer._comon.dependency_inject.modules.AppModule
import com.example.mediaplayer._comon.dependency_inject.modules.RepositoriesModule
import com.example.mediaplayer.presentation.AppApplication
import com.example.mediaplayer.presentation.MainActivity
import dagger.BindsInstance
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(
    modules = [
        RepositoriesModule::class,
        AppModule::class,
    ]
)
interface AppComponent {

    fun inject(appApplication: AppApplication)
    fun inject(mainActivity: MainActivity)

    @Component.Builder
    interface Builder {
        @BindsInstance
        fun addContext(context: Context): Builder
        fun build(): AppComponent
    }
}