package tree.ralph.mindmapmemo.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.components.SingletonComponent
import tree.ralph.mindmapmemo.data.repository.MindMapRepository
import tree.ralph.mindmapmemo.data.repository.MindMapRepositoryImpl
import tree.ralph.mindmapmemo.data.repository.OpenProtocolRepository
import tree.ralph.mindmapmemo.data.repository.OpenProtocolRepositoryImpl
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Singleton
    @Provides
    fun provideMindMapRepository(
        mindMapRepositoryImpl: MindMapRepositoryImpl
    ): MindMapRepository = mindMapRepositoryImpl

    @Singleton
    @Provides
    fun provideOpenProtocolRepository(
        openProtocolRepositoryImpl: OpenProtocolRepositoryImpl
    ): OpenProtocolRepository = openProtocolRepositoryImpl
}
