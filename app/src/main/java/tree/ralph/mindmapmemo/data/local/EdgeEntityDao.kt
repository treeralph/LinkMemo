package tree.ralph.mindmapmemo.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import tree.ralph.mindmapmemo.data.local.model.EdgeEntity

@Dao
interface EdgeEntityDao {
    @Query("INSERT INTO EdgeEntity (node1, node2, folder) VALUES (:node1, :node2, :folder)")
    fun insertEdgeEntity(node1: Long, node2: Long, folder: Long): Long
    @Insert
    fun insertEdgeEntities(edges: List<EdgeEntity>)
    @Update
    fun updateEdgeEntities(edges: List<EdgeEntity>)
    @Delete
    fun deleteEdgeEntities(edges: List<EdgeEntity>)
    @Query("SELECT * FROM EdgeEntity")
    fun getAllEdgeEntities(): List<EdgeEntity>
    @Query("SELECT * FROM EdgeEntity WHERE id = :id")
    fun getEdgeEntityById(id: Long): EdgeEntity
    @Query("DELETE FROM EdgeEntity WHERE node1=:id OR node2=:id")
    fun deleteEdgeEntitiesByNodeId(id: Long)
    @Query("SELECT * FROM EdgeEntity WHERE folder = :folder")
    fun getEdgeEntitiesByFolder(folder: Long): List<EdgeEntity>
    @Query("SELECT * FROM EdgeEntity WHERE (node1 = :node1 AND node2 = :node2) OR (node1 = :node2 AND node2 = :node1)")
    fun isEdgeEntity(node1: Long, node2: Long): List<EdgeEntity>
}