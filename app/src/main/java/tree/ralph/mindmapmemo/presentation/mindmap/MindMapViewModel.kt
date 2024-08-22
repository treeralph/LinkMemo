package tree.ralph.mindmapmemo.presentation.mindmap

import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.yield
import tree.ralph.mindmapmemo.data.local.model.DataEntity
import tree.ralph.mindmapmemo.data.local.model.EdgeEntity
import tree.ralph.mindmapmemo.data.local.model.NodeEntity
import tree.ralph.mindmapmemo.data.repository.MindMapRepository
import tree.ralph.mindmapmemo.data.repository.OpenProtocolRepository
import javax.inject.Inject
import kotlin.random.Random
import kotlin.time.measureTime

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

    private val _nodeEntityStates = mutableListOf<MutableState<NodeEntity>>()
    private val _dataEntityStates = mutableListOf<MutableState<DataEntity>>()
    private val _edgeEntityStates = mutableListOf<MutableState<EdgeEntity>>()
    private val _notificationNodeEntityState = mutableStateOf<NodeEntity?>(null)

    val nodeEntityStates: List<State<NodeEntity>> = _nodeEntityStates
    val dataEntityStates: List<State<DataEntity>> = _dataEntityStates
    val edgeEntityStates: List<State<EdgeEntity>> = _edgeEntityStates
    val notificationNodeEntityState: State<NodeEntity?> = _notificationNodeEntityState

    private var drawJob: Job? = null

    private var onMovedNodeEntity: NodeEntity? = null

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
        Log.e("URGENT_TAG", "MindMapViewModel: init")

        viewModelScope.launch(Dispatchers.IO) {
            val nodeJob = launch {
                val nodeList = mindMapRepository.getAllNodesByFolder()
                nodeList.forEach { node ->
                    val nodeEntity = node.nodeEntity
                    nodeId2Index[nodeEntity.id] = nodeEntities.size
                    nodeEntities.add(nodeEntity)

                    _nodeEntityStates.add(mutableStateOf(nodeEntity))
                    _dataEntityStates.add(mutableStateOf(node.dataEntity))
                }
            }
            val edgeJob = launch {
                val edgeList = mindMapRepository.getAllEdgesByFolder()
                edgeList.forEach { edge ->
                    edgeEntities.add(edge)
                    _edgeEntityStates.add(mutableStateOf(edge))
                }
            }
            nodeJob.join()
            edgeJob.join()
            draw()
        }
    }

    private fun draw() {
        drawJob = viewModelScope.launch(
            Dispatchers.Default + drawCoroutineExceptionHandler
        ) {
            while(true) {
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

                yield()
            }
        }
    }

    fun onNodeDragStart(nodeEntity: NodeEntity) {
        viewModelScope.launch {
            drawJob?.cancel()
            onMovedNodeEntity = nodeEntity
        }
    }

    fun onNodeMoved(index: Int, offset: Offset) {
        val target = _nodeEntityStates[index].value
        val x = target.x + offset.x
        val y = target.y + offset.y
        _nodeEntityStates[index].value = target.copy(x = x, y = y)
        findCollisionNode(index, x, y)
    }

    private fun findCollisionNode(selfIndex: Int, selfX: Double, selfY: Double) {
        var targetId = -1
        nodeEntities.forEachIndexed { index, nodeEntity ->
            if(isCollision(nodeEntity.x, nodeEntity.y, selfX, selfY)) {
                if(index != selfIndex) {
                    targetId = index
                }
            }
        }
        _notificationNodeEntityState.value = null
        if(targetId != -1) {
            _notificationNodeEntityState.value = nodeEntities[targetId].copy()
        }
    }

    fun onNodeDragEnd(index: Int) {
        viewModelScope.launch {
            _notificationNodeEntityState.value?.let {
                onMovedNodeEntity?.let { movedEntity ->
                    launch(Dispatchers.IO) {
                        addEdgeEntity(movedEntity.id, it.id) { edgeEntity ->
                            edgeEntities.add(edgeEntity)
                            _edgeEntityStates.add(mutableStateOf(edgeEntity))
                        }
                    }

                    onMovedNodeEntity = null
                    _notificationNodeEntityState.value = null
                    nodeEntities[index] = _nodeEntityStates[index].value

                    // todo: 순서대로 잘 출력 되는지 확인 할 것
                    Log.e("URGENT_TAG", "onNodeDragEnd: 1")
                }
            }

            Log.e("URGENT_TAG", "onNodeDragEnd: 2")
            draw()
        }
    }

    private fun addEdgeEntity(node1: Long, node2: Long, callback: (EdgeEntity) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            if (mindMapRepository.isEdgeEntity(node1, node2).isEmpty()) {
                val id = mindMapRepository.insertEdgeEntity(node1, node2)

                val node1Index = nodeId2Index[node1]!!
                val node2Index = nodeId2Index[node2]!!

                val tempNode1 = nodeEntities[node1Index]
                val tempNode2 = nodeEntities[node2Index]
                nodeEntities[node1Index] = tempNode1.copy(mass = tempNode1.mass + 1)
                nodeEntities[node2Index] = tempNode2.copy(mass = tempNode2.mass + 1)

                callback(mindMapRepository.getEdgeById(id))
            }
        }
    }


    fun addNode() {
        viewModelScope.launch {
            val temp = launch(Dispatchers.IO) {

                val link = _addNodeDialogUiState.value.content
                val response = openProtocolRepository.getResponse(link)

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

            temp.join()
            _isAddNodeDialog.value = false
            draw()
        }
    }

    fun getNodeEntity(nodeId: Long) = _nodeEntityStates[nodeId2Index[nodeId]!!].value

    fun getDataEntityState(index: Int) = _dataEntityStates[index]

    fun openAddNodeDialog() {
        viewModelScope.launch {
            drawJob?.cancelAndJoin()
            _isAddNodeDialog.value = true
        }
    }

    fun closeAddNodeDialog() {
        _isAddNodeDialog.value = false
        _addNodeDialogUiState.value = DialogUiState()
        draw()
    }

    fun onTitleChanged(new: String) {
        _addNodeDialogUiState.value = _addNodeDialogUiState.value.copy(content = new)
    }

    fun releaseDetailNode() {
        _currentDetailNode.value = null
    }



    private val drawCoroutineExceptionHandler = CoroutineExceptionHandler { context, throwable ->
        Log.e("URGENT_TAG", "drawCoroutineExceptionHandler: $throwable")
    }

    private external suspend fun operate(
        nodes: ArrayList<NodeEntity>,
        edges: ArrayList<EdgeEntity>,
        nodeId2Index: HashMap<Long, Int>
    )
}