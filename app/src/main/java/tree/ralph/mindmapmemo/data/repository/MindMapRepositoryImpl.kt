package tree.ralph.mindmapmemo.data.repository

import androidx.compose.runtime.MutableState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import tree.ralph.mindmapmemo.data.local.AppDatabase
import tree.ralph.mindmapmemo.data.local.DataEntityDao
import tree.ralph.mindmapmemo.data.local.EdgeEntityDao
import tree.ralph.mindmapmemo.data.local.FolderDao
import tree.ralph.mindmapmemo.data.local.NodeEntityDao
import tree.ralph.mindmapmemo.data.local.model.DataEntity
import tree.ralph.mindmapmemo.data.local.model.EdgeEntity
import tree.ralph.mindmapmemo.data.local.model.Folder
import tree.ralph.mindmapmemo.data.local.model.Node
import tree.ralph.mindmapmemo.data.local.model.NodeEntity
import javax.inject.Inject

class MindMapRepositoryImpl @Inject constructor(
    private val db: AppDatabase,
    private val nodeEntityDao: NodeEntityDao,
    private val dataEntityDao: DataEntityDao,
    private val edgeEntityDao: EdgeEntityDao,
    private val folderDao: FolderDao
): MindMapRepository {

    private var _currentFolder = MutableStateFlow<Folder?>(null)
    override val currentFolder = _currentFolder.asStateFlow()

    init {

    }

    override fun onChangeCurrentFolder(folder: Folder) {
        _currentFolder.value = folder
    }

    override suspend fun getAllNodesByFolder(): List<Node> {
        return nodeEntityDao.getAllNodesByFolder(currentFolder.value!!.id)
    }

    override suspend fun getNodeById(
        id: Long
    ) = nodeEntityDao.getNodeById(id)

    override suspend fun isEdgeEntity(
        node1: Long,
        node2: Long
    ) = edgeEntityDao.isEdgeEntity(node1, node2)

    override suspend fun insertEdgeEntity(
        node1: Long,
        node2: Long,
    ) = edgeEntityDao.insertEdgeEntity(node1, node2, currentFolder.value!!.id)

    override suspend fun getEdgeById(id: Long) = edgeEntityDao.getEdgeEntityById(id)

    override suspend fun insertNode(
        nodeEntity: NodeEntity,
        dataEntity: DataEntity
    ): Long {
        var nodeEntityId = -1L
        db.runInTransaction {
            nodeEntityId = nodeEntityDao.insertNodeEntity(
                nodeEntity.copy(folder = currentFolder.value!!.id)
            )
            dataEntityDao.insertDataEntity(dataEntity.copy(nodeEntityId = nodeEntityId))
        }
        return nodeEntityId
    }

    override suspend fun insertNode(
        nodeEntity: NodeEntity,
        dataEntity: DataEntity,
        folderId: Long,
    ): Long {
        var nodeEntityId = -1L
        db.runInTransaction {
            nodeEntityId = nodeEntityDao.insertNodeEntity(
                nodeEntity.copy(folder = folderId)
            )
            dataEntityDao.insertDataEntity(dataEntity.copy(nodeEntityId = nodeEntityId))
        }
        return nodeEntityId
    }

    override fun getAllFolders() = folderDao.getAllFolders()
    override suspend fun insertFolder(
        folderName: String,
        folderInfo: String
    ) = folderDao.insertFolder(folderName, folderInfo)
    override suspend fun deleteFolder(folder: Folder) = folderDao.deleteFolder(folder)
    override suspend fun updateFolder(folder: Folder) = folderDao.updateFolder(folder)
    override suspend fun getFoldersByName(
        folderName: String
    ) = folderDao.getFoldersByName(folderName)
}
