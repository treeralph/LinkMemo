package tree.ralph.mindmapmemo.data.local.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Folder(
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0,
    @ColumnInfo(defaultValue = "")
    var folderName: String = "",
    @ColumnInfo(defaultValue = "")
    var folderInfo: String = "",
    @ColumnInfo(defaultValue = "CURRENT_TIMESTAMP")
    var createdTime: String = ""
)
