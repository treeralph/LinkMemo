package tree.ralph.mindmapmemo.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import tree.ralph.mindmapmemo.data.repository.LinkBumperRepository
import tree.ralph.mindmapmemo.data.repository.LinkBumperRepositoryImpl
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

    @Singleton
    @Provides
    fun provideContextToLinkBumperRepository(
        @ApplicationContext context: Context
    ): LinkBumperRepositoryImpl = LinkBumperRepositoryImpl(context)

    @Singleton
    @Provides
    fun provideLinkBumperRepository(
        linkBumperRepositoryImpl: LinkBumperRepositoryImpl
    ): LinkBumperRepository = linkBumperRepositoryImpl
}
