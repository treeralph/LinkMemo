package tree.ralph.mindmapmemo.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import tree.ralph.mindmapmemo.data.local.model.DataEntity
import tree.ralph.mindmapmemo.data.local.model.EdgeEntity
import tree.ralph.mindmapmemo.data.local.model.Folder
import tree.ralph.mindmapmemo.data.local.model.Node
import tree.ralph.mindmapmemo.data.local.model.NodeEntity

interface MindMapRepository {
    val currentFolder: StateFlow<Folder?>
    fun onChangeCurrentFolder(folder: Folder)

    suspend fun getAllNodesByFolder(): List<Node>
    suspend fun getNodeById(id: Long): Node

    suspend fun isEdgeEntity(node1: Long, node2: Long): List<EdgeEntity>
    suspend fun insertEdgeEntity(node1: Long, node2: Long): Long
    suspend fun getEdgeById(id: Long): EdgeEntity
    suspend fun getAllEdgesByFolder(): List<EdgeEntity>

    suspend fun insertNode(nodeEntity: NodeEntity, dataEntity: DataEntity): Long
    suspend fun insertNode(nodeEntity: NodeEntity, dataEntity: DataEntity, folderId: Long): Long

    fun getAllFolders(): Flow<List<Folder>>
    suspend fun insertFolder(folderName: String, folderInfo: String): Long
    suspend fun deleteFolder(folder: Folder)
    suspend fun updateFolder(folder: Folder)
    suspend fun getFoldersByName(folderName: String): List<Folder>
}