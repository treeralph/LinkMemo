package tree.ralph.mindmapmemo.data.local.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import tree.ralph.mindmapmemo.data.local.AppDatabase
import tree.ralph.mindmapmemo.data.local.DataEntityDao
import tree.ralph.mindmapmemo.data.local.EdgeEntityDao
import tree.ralph.mindmapmemo.data.local.FolderDao
import tree.ralph.mindmapmemo.data.local.NodeEntityDao
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object LocalModule {

    @Singleton
    @Provides
    fun provideAppDatabase(
        @ApplicationContext context: Context
    ): AppDatabase = Room.databaseBuilder(
        context,
        AppDatabase::class.java,
        "app.db"
    ).build()

    @Singleton
    @Provides
    fun provideNodeEntityDao(
        appDatabase: AppDatabase
    ): NodeEntityDao = appDatabase.nodeEntityDao()

    @Singleton
    @Provides
    fun provideDataEntityDao(
        appDatabase: AppDatabase
    ): DataEntityDao = appDatabase.dataEntityDao()

    @Singleton
    @Provides
    fun provideEdgeEntityDao(
        appDatabase: AppDatabase
    ): EdgeEntityDao = appDatabase.edgeEntityDao()

    @Singleton
    @Provides
    fun provideFolderDao(
        appDatabase: AppDatabase
    ): FolderDao = appDatabase.folderDao()
}