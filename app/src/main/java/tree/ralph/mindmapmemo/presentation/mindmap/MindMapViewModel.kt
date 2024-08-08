package tree.ralph.mindmapmemo.presentation.mindmap

import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import tree.ralph.mindmapmemo.data.local.model.DataEntity
import tree.ralph.mindmapmemo.data.local.model.EdgeEntity
import tree.ralph.mindmapmemo.data.local.model.Folder
import tree.ralph.mindmapmemo.data.local.model.Node
import tree.ralph.mindmapmemo.data.local.model.NodeEntity
import tree.ralph.mindmapmemo.data.remote.model.OpenProtocolResponse
import tree.ralph.mindmapmemo.data.repository.MindMapRepository
import tree.ralph.mindmapmemo.data.repository.OpenProtocolRepository
import javax.inject.Inject
import kotlin.math.min
import kotlin.random.Random

data class DialogUiState(
    val content: String = "",
    val isError: Boolean = false,
    val errorMessage: String = ""
)

@HiltViewModel
class MindMapViewModel @Inject constructor(
    private val mindMapRepository: MindMapRepository,
    private val openProtocolRepository: OpenProtocolRepository
): ViewModel() {

    private val nodeEntities = ArrayList<NodeEntity>()
    private val edgeEntities = ArrayList<EdgeEntity>()
    private val nodeId2Index = HashMap<Long, Int>()

    private val mutex = Mutex()

    private var onMovedNodeEntity: NodeEntity? = null

    private val _nodeEntityStates = mutableListOf<MutableState<NodeEntity>>()
    private val _dataEntityStates = mutableListOf<MutableState<DataEntity>>()
    private val _edgeEntityStates = mutableListOf<MutableState<EdgeEntity>>()
    private val _notificationNodeEntityState = mutableStateOf<NodeEntity?>(null)

    val nodeEntityStates: List<MutableState<NodeEntity>> = _nodeEntityStates
    val dataEntityStates: List<MutableState<DataEntity>> = _dataEntityStates
    val edgeEntityStates: List<MutableState<EdgeEntity>> = _edgeEntityStates
    val notificationNodeEntityState = _notificationNodeEntityState

    private val _opState = mutableStateOf(true)
    val opState: State<Boolean> = _opState

    val currentFolder = mindMapRepository.currentFolder


    /** start Add Node Dialog */

    private val _isAddNodeDialog = mutableStateOf(false)
    val isAddNodeDialog: State<Boolean> = _isAddNodeDialog

    private val _addNodeDialogUiState = mutableStateOf(DialogUiState())
    val addNodeDialogUiState: State<DialogUiState> = _addNodeDialogUiState

    /** end Add Node Dialog */

    /** start Node Detail Dialog */

    private val _currentDetailNode = mutableStateOf<DataEntity?>(null)
    val currentDetailNode: State<DataEntity?> = _currentDetailNode

    val isNodeDetailDialog = derivedStateOf { _currentDetailNode.value != null }

    /** end Node Detail Dialog */



    init {

    }

    fun draw() {
        _opState.value = true
        viewModelScope.launch(Dispatchers.IO) {
            while(_opState.value) {
                mutex.withLock {
                    operate(
                        nodes = nodeEntities,
                        edges = edgeEntities,
                        nodeId2Index = nodeId2Index
                    )
                    viewModelScope.launch(Dispatchers.Main) {
                        nodeEntities.forEachIndexed { index, nodeEntity ->
                            _nodeEntityStates[index].value = nodeEntity.copy()
                        }
                    }
                }
            }
        }
    }





    /**
     * Drag
     * */

    fun onNodeDragStart(nodeEntity: NodeEntity) {
        viewModelScope.launch {
            mutex.lock()
            onMovedNodeEntity = nodeEntity
        }
    }

    fun onNodeMoved(index: Int, offset: Offset) {

        val target = _nodeEntityStates[index].value
        val x = target.x + offset.x
        val y = target.y + offset.y

        val temp = target.copy(x = x, y = y)

        nodeEntities[index] = temp
        _nodeEntityStates[index].value = temp

        findCollisionNodeEntity(temp.id, temp.x, temp.y)?.let {
            _notificationNodeEntityState.value = it
        }
    }

    private fun findCollisionNodeEntity(selfId: Long, selfX: Double, selfY: Double): NodeEntity? {
        return nodeEntities.firstOrNull {
            it.id != selfId && isCollision(it.x, it.y, selfX, selfY)
        }
    }

    fun onNodeDragEnd() {
        viewModelScope.launch {
            _notificationNodeEntityState.value?.let {
                onMovedNodeEntity?.let { movedEntity ->
                    addEdgeEntity(movedEntity.id, it.id) { edgeEntity ->
                        edgeEntities.add(edgeEntity)
                        _edgeEntityStates.add(mutableStateOf(edgeEntity))
                    }
                }
            }
            onMovedNodeEntity = null
            _notificationNodeEntityState.value = null

            mutex.unlock()
            draw()
        }
    }

    private fun addEdgeEntity(node1: Long, node2: Long, callback: (EdgeEntity) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            if (mindMapRepository.isEdgeEntity(node1, node2).isEmpty()) {
                val id = mindMapRepository.insertEdgeEntity(node1, node2)
                nodeEntities[nodeId2Index[node1]!!].mass += 1
                nodeEntities[nodeId2Index[node2]!!].mass += 1
                callback(mindMapRepository.getEdgeById(id))
            }
        }
    }

    /**
     * /Drag
     * */

    fun addNode() {
        viewModelScope.launch {
            _opState.value = false
            val link = _addNodeDialogUiState.value.content
            mutex.withLock {
                val response = openProtocolRepository.getResponse(link)
                subAddNode(link, response)
            }
        }
    }

    private fun subAddNode(link: String, response: OpenProtocolResponse) {
        viewModelScope.launch(Dispatchers.IO) {

            val nodeEntity = NodeEntity(
                x = Random.nextDouble(-30.0, 30.0),
                y = Random.nextDouble(-30.0, 30.0),
            )

            val dataEntity = DataEntity(
                imgUri = response.imageUrl,
                linkUrl = link,
                content = response.title,
                description = response.description,
            )

            val nodeId = mindMapRepository.insertNode(
                nodeEntity = nodeEntity,
                dataEntity = dataEntity
            )

            val node = mindMapRepository.getNodeById(nodeId)
            nodeEntities.add(node.nodeEntity)
            _nodeEntityStates.add(mutableStateOf(node.nodeEntity))
            _dataEntityStates.add(mutableStateOf(node.dataEntity))
            nodeId2Index[nodeId] = nodeEntities.size - 1
        }
    }

    fun getNodeEntity(nodeId: Long) = _nodeEntityStates[nodeId2Index[nodeId]!!].value

    fun getDataEntityState(index: Int) = _dataEntityStates[index]

    fun openAddNodeDialog() {
        _isAddNodeDialog.value = true
    }

    fun closeAddNodeDialog() {
        _addNodeDialogUiState.value = DialogUiState()
        _isAddNodeDialog.value = false
    }

    fun onTitleChanged(new: String) {
        _addNodeDialogUiState.value = _addNodeDialogUiState.value.copy(content = new)
    }


    fun releaseDetailNode() {
        _currentDetailNode.value = null
    }

    private external suspend fun operate(
        nodes: ArrayList<NodeEntity>,
        edges: ArrayList<EdgeEntity>,
        nodeId2Index: HashMap<Long, Int>
    )
}
