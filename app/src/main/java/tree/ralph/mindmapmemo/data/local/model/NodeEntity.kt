package tree.ralph.mindmapmemo.data.local.model

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Stable
@Entity
data class NodeEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(defaultValue = "0.0")
    var dx: Double = 0.0,
    @ColumnInfo(defaultValue = "0.0")
    var dy: Double = 0.0,
    @ColumnInfo(defaultValue = "0.0")
    var old_dx: Double = 0.0,
    @ColumnInfo(defaultValue = "0.0")
    var old_dy: Double = 0.0,
    @ColumnInfo(defaultValue = "1.0")
    val mass: Double = 1.0,
    @ColumnInfo(defaultValue = "0.0")
    var x: Double = 0.0,
    @ColumnInfo(defaultValue = "0.0")
    var y: Double = 0.0,
    @ColumnInfo(defaultValue = "${NODE_RADIUS * 2}")
    val size: Double = NODE_RADIUS * 2,
    @ColumnInfo(defaultValue = "1.0")
    val weight: Double = 1.0,
    @ColumnInfo(defaultValue = "-1")
    val folder: Long = -1,
)
