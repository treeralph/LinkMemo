package tree.ralph.mindmapmemo.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import tree.ralph.mindmapmemo.data.local.model.Folder

@Dao
interface FolderDao {

    @Query("SELECT * FROM Folder")
    fun getAllFolders(): Flow<List<Folder>>

    @Query("INSERT INTO Folder(folderName, folderInfo) VALUES(:folderName, :folderInfo)")
    suspend fun insertFolder(folderName: String, folderInfo: String): Long

    @Delete
    suspend fun deleteFolder(folder: Folder)

    @Update
    suspend fun updateFolder(folder: Folder)

    @Query("SELECT * FROM Folder WHERE folderName = :folderName")
    suspend fun getFoldersByName(folderName: String): List<Folder>
}