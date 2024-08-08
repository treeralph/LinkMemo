package tree.ralph.mindmapmemo.data.local.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class DataEntity(
    @PrimaryKey(autoGenerate = true)
    var id: Long = -1,
    @ColumnInfo(defaultValue = "CURRENT_TIMESTAMP")
    var createdTime: String = "",
    @ColumnInfo(defaultValue = "")
    var imgUri: String = "",
    @ColumnInfo(defaultValue = "")
    var linkUrl: String = "",
    @ColumnInfo(defaultValue = "")
    var content: String = "",
    @ColumnInfo(defaultValue = "")
    var description: String = "",
    @ColumnInfo(defaultValue = "")
    var nodeInfo: String = "",
    @ColumnInfo(defaultValue = "")
    var nodeColor: String = "",
    @ColumnInfo(defaultValue = "-1")
    var nodeEntityId: Long = -1
)
