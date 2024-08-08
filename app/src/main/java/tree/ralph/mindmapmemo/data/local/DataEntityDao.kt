package tree.ralph.mindmapmemo.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import androidx.room.Upsert
import tree.ralph.mindmapmemo.data.local.model.DataEntity
import tree.ralph.mindmapmemo.data.local.model.NodeEntity

@Dao
interface DataEntityDao {
    @Query("SELECT * FROM DataEntity")
    fun getAllDataEntities(): List<DataEntity>

    @Insert
    fun insertDataEntity(nodeEntity: DataEntity): Long

    @Delete
    fun deleteDataEntity(nodeEntity: DataEntity)

    @Update
    fun updateDataEntity(nodeEntity: DataEntity)

    @Upsert
    fun upsertDataEntity(nodeEntity: DataEntity): Long
}