package tree.ralph.mindmapmemo.data.local.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class EdgeEntity(
    @PrimaryKey(autoGenerate = true)
    var id: Long = -1,
    @ColumnInfo(defaultValue = "-1")
    var node1: Long = 0,
    @ColumnInfo(defaultValue = "-1")
    var node2: Long = 0,
    @ColumnInfo(defaultValue = "1.0")
    var weight: Double = 1.0,
    @ColumnInfo(defaultValue = "CURRENT_TIMESTAMP")
    var createdTime: String = "",
    @ColumnInfo(defaultValue = "-1")
    var folder: Long = -1
)
