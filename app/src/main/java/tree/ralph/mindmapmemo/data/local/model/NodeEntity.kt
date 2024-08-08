package tree.ralph.mindmapmemo.data.local.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity
data class NodeEntity(
    @PrimaryKey(autoGenerate = true)
    var id: Long = -1,
    @ColumnInfo(defaultValue = "0.0")
    var dx: Double = 0.0,
    @ColumnInfo(defaultValue = "0.0")
    var dy: Double = 0.0,
    @ColumnInfo(defaultValue = "0.0")
    var old_dx: Double = 0.0,
    @ColumnInfo(defaultValue = "0.0")
    var old_dy: Double = 0.0,
    @ColumnInfo(defaultValue = "1.0")
    var mass: Double = 1.0,
    @ColumnInfo(defaultValue = "0.0")
    var x: Double = 0.0,
    @ColumnInfo(defaultValue = "0.0")
    var y: Double = 0.0,
    @ColumnInfo(defaultValue = "${NODE_RADIUS * 2}")
    var size: Double = NODE_RADIUS * 2,
    @ColumnInfo(defaultValue = "1.0")
    var weight: Double = 1.0,
    @ColumnInfo(defaultValue = "-1")
    var folder: Long = -1,
)
