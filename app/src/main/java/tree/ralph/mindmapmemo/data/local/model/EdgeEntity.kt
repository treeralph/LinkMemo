package tree.ralph.mindmapmemo.data.local.model

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class EdgeEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(defaultValue = "-1")
    val node1: Long = 0,
    @ColumnInfo(defaultValue = "-1")
    val node2: Long = 0,
    @ColumnInfo(defaultValue = "1.0")
    val weight: Double = 1.0,
    @ColumnInfo(defaultValue = "CURRENT_TIMESTAMP")
    val createdTime: String = "",
    @ColumnInfo(defaultValue = "-1")
    val folder: Long = -1
)
