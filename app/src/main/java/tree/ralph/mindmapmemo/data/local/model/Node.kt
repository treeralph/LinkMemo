package tree.ralph.mindmapmemo.data.local.model

import androidx.room.Embedded
import androidx.room.Relation

data class Node(
    @Embedded val nodeEntity: NodeEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "nodeEntityId"
    ) val dataEntity: DataEntity
)


/**
 *
 * Node = NodeEntity + DataEntity
 *
 * */