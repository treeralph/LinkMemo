package tree.ralph.mindmapmemo.data.remote.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import tree.ralph.mindmapmemo.data.remote.OpenProtocolDataSource
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RemoteModule {
    @Singleton
    @Provides
    fun provideOpenProtocolDatasource() = OpenProtocolDataSource()
}
