package tree.ralph.mindmapmemo.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import androidx.room.Upsert
import tree.ralph.mindmapmemo.data.local.model.Node
import tree.ralph.mindmapmemo.data.local.model.NodeEntity

@Dao
interface NodeEntityDao {

    @Query("SELECT * FROM NodeEntity")
    fun getAllNodeEntities(): List<NodeEntity>

    @Insert
    fun insertNodeEntity(nodeEntity: NodeEntity): Long

    @Delete
    fun deleteNodeEntity(nodeEntity: NodeEntity)

    @Update
    fun updateNodeEntity(nodeEntity: NodeEntity)

    @Upsert
    fun upsertNodeEntity(nodeEntity: NodeEntity): Long

    @Query("DELETE FROM NodeEntity WHERE id = :id")
    fun deleteNodeEntityById(id: Long)

    @Transaction
    @Query("SELECT * FROM NodeEntity WHERE folder = :folderId")
    fun getAllNodesByFolder(folderId: Long): List<Node>

    @Transaction
    @Query("SELECT * FROM NodeEntity WHERE id = :id")
    fun getNodeById(id: Long): Node

}
