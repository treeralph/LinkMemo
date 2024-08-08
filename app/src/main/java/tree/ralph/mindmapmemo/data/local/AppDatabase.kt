package tree.ralph.mindmapmemo.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import tree.ralph.mindmapmemo.data.local.model.DataEntity
import tree.ralph.mindmapmemo.data.local.model.EdgeEntity
import tree.ralph.mindmapmemo.data.local.model.Folder
import tree.ralph.mindmapmemo.data.local.model.NodeEntity

@Database(
    entities = [
        NodeEntity::class,
        DataEntity::class,
        EdgeEntity::class,
        Folder::class
    ],
    version = 1
)
abstract class AppDatabase: RoomDatabase() {
    abstract fun nodeEntityDao(): NodeEntityDao
    abstract fun dataEntityDao(): DataEntityDao
    abstract fun edgeEntityDao(): EdgeEntityDao
    abstract fun folderDao(): FolderDao
}
