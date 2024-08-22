package tree.ralph.mindmapmemo.data.local.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class DataEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(defaultValue = "CURRENT_TIMESTAMP")
    val createdTime: String = "",
    @ColumnInfo(defaultValue = "")
    val imgUri: String = "",
    @ColumnInfo(defaultValue = "")
    val linkUrl: String = "",
    @ColumnInfo(defaultValue = "")
    val content: String = "",
    @ColumnInfo(defaultValue = "")
    val description: String = "",
    @ColumnInfo(defaultValue = "")
    val nodeInfo: String = "",
    @ColumnInfo(defaultValue = "")
    val nodeColor: String = "",
    @ColumnInfo(defaultValue = "-1")
    val nodeEntityId: Long = -1
)
