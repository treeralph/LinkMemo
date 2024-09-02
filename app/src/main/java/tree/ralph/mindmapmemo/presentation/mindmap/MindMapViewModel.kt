package tree.ralph.mindmapmemo.presentation.mindmap

import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import kotlinx.coroutines.selects.select
import kotlinx.coroutines.yield
import tree.ralph.mindmapmemo.data.local.model.DataEntity
import tree.ralph.mindmapmemo.data.local.model.EdgeEntity
import tree.ralph.mindmapmemo.data.local.model.LinkBumper
import tree.ralph.mindmapmemo.data.local.model.NodeEntity
import tree.ralph.mindmapmemo.data.repository.LinkBumperRepository
import tree.ralph.mindmapmemo.data.repository.MindMapRepository
import tree.ralph.mindmapmemo.data.repository.OpenProtocolRepository
import javax.inject.Inject
import kotlin.math.PI
import kotlin.math.cos
import kotlin.random.Random

data class DialogUiState(
    val content: String = "",
    val isError: Boolean = false,
    val errorMessage: String = "",
)

@HiltViewModel
class MindMapViewModel @Inject constructor(
    private val mindMapRepository: MindMapRepository,
    private val openProtocolRepository: OpenProtocolRepository,
    private val linkBumperRepository: LinkBumperRepository
) : ViewModel() {

    private val nodeEntities = ArrayList<NodeEntity>()
    private val edgeEntities = ArrayList<EdgeEntity>()
    private val nodeId2Index = HashMap<Long, Int>()

    private val _nodeEntityStates = mutableStateListOf<NodeEntity>()
    private val _dataEntityStates = mutableStateListOf<DataEntity>()
    private val _edgeEntityStates = mutableStateListOf<EdgeEntity>()
    private val _notificationNodeEntityState = mutableStateOf<NodeEntity?>(null)

    val nodeEntityStates: List<NodeEntity> = _nodeEntityStates
    val dataEntityStates: List<DataEntity> = _dataEntityStates
    val edgeEntityStates: List<EdgeEntity> = _edgeEntityStates
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

    private val _isNodeDetailDialog = mutableStateOf(false)
    val isNodeDetailDialog: State<Boolean> = _isNodeDetailDialog

    // val isNodeDetailDialog = derivedStateOf { _currentDetailNode.value != null }

    /** end Node Detail Dialog */

    init {
        viewModelScope.launch(Dispatchers.IO) {
            val tmp1 = launch {
                val nodeList = mindMapRepository.getAllNodesByFolder()
                nodeList.forEach { node ->
                    val nodeEntity = node.nodeEntity

                    nodeId2Index[nodeEntity.id] = nodeEntities.size
                    nodeEntities.add(nodeEntity)

                    launch(Dispatchers.Main) {
                        _nodeEntityStates.add(nodeEntity)
                        _dataEntityStates.add(node.dataEntity)
                    }
                }
            }

            val tmp2 = launch {
                val edgeList = mindMapRepository.getAllEdgesByFolder()
                edgeList.forEach { edge ->
                    edgeEntities.add(edge)

                    launch(Dispatchers.Main) {
                        _edgeEntityStates.add(edge)
                    }
                }
            }

            tmp1.join()
            tmp2.join()
            draw()
        }
    }

    private fun draw() {

        // drawJob이 cancel 되기 전에 다른 코루틴 런처가 정의될 수 있는 가능성을 막는 코드 필요

        drawJob = viewModelScope.launch(Dispatchers.Default + drawCoroutineExceptionHandler) {
            while (true) {
                operate(
                    nodes = nodeEntities,
                    edges = edgeEntities,
                    nodeId2Index = nodeId2Index
                )
                launch(Dispatchers.Main) {
                    nodeEntities.forEachIndexed { index, nodeEntity ->
                        _nodeEntityStates[index] = nodeEntity.copy()
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
        val target = _nodeEntityStates[index]
        val x = target.x + offset.x
        val y = target.y + offset.y
        _nodeEntityStates[index] = target.copy(x = x, y = y)
        findCollisionNode(index, x, y)
    }

    private fun findCollisionNode(selfIndex: Int, selfX: Double, selfY: Double) {
        var targetId = -1
        nodeEntities.forEachIndexed { index, nodeEntity ->
            if (isCollision(nodeEntity.x, nodeEntity.y, selfX, selfY)) {
                if (index != selfIndex) {
                    targetId = index
                }
            }
        }
        _notificationNodeEntityState.value = null
        if (targetId != -1) {
            _notificationNodeEntityState.value = nodeEntities[targetId].copy()
        }
    }

    fun onNodeDragEnd(index: Int) {
        viewModelScope.launch {
            onMovedNodeEntity?.let { movedEntity ->
                _nodeEntityStates[index] = onMovedNodeEntity!!.copy()
                nodeEntities[index] = onMovedNodeEntity!!.copy()
                _notificationNodeEntityState.value?.let {
                    val tmp = launch(Dispatchers.IO) {
                        addEdgeEntity(movedEntity.id, it.id) { edgeEntity ->
                            edgeEntities.add(edgeEntity)
                            _edgeEntityStates.add(edgeEntity)
                        }
                    }
                    tmp.join()
                    onMovedNodeEntity = null
                    _notificationNodeEntityState.value = null
                }
            }
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
                _nodeEntityStates.add(node.nodeEntity)
                _dataEntityStates.add(node.dataEntity)
                nodeId2Index[nodeId] = nodeEntities.size - 1
            }

            temp.join()
            _isAddNodeDialog.value = false
            draw()
        }
    }

    fun getNodeEntity(nodeId: Long) = _nodeEntityStates[nodeId2Index[nodeId]!!]

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
        _isNodeDetailDialog.value = false
        _currentDetailNode.value = null
        draw()
    }

    fun openDetailNode(dataEntityIndex: Int) {
        viewModelScope.launch {
            drawJob?.cancelAndJoin()
            _isNodeDetailDialog.value = true
            _currentDetailNode.value = _dataEntityStates[dataEntityIndex]
        }
    }

    private val drawCoroutineExceptionHandler = CoroutineExceptionHandler { context, throwable ->
        Log.e("URGENT_TAG", "drawCoroutineExceptionHandler: $throwable")
    }

    private external suspend fun operate(
        nodes: ArrayList<NodeEntity>,
        edges: ArrayList<EdgeEntity>,
        nodeId2Index: HashMap<Long, Int>,
    )

    /**
     * @Test
     * Delete Node
     * */

    private fun deleteNode(
        nodeEntity: NodeEntity,
        parentJob: Job
    ) = viewModelScope.launch(Dispatchers.IO + parentJob) {

        deleteEdgesByNodeId(nodeEntity.id, currentCoroutineContext().job)
        mindMapRepository.deleteNode(nodeEntity, _dataEntityStates.first { it.id == nodeEntity.id })

        val targetNodeIndex = nodeEntities.size - 1
        swapNode(nodeId2Index[nodeEntity.id]!!, targetNodeIndex)

        launch(Dispatchers.Main) {
            nodeEntities.removeAt(targetNodeIndex)
            _nodeEntityStates.removeAt(targetNodeIndex)
            _dataEntityStates.removeAt(targetNodeIndex)
        }
    }

    private suspend fun deleteEdgesByNodeId(nodeId: Long, parentJob: Job) {

        val predicate: (EdgeEntity) -> Boolean = {
            it.node1 == nodeId || it.node2 == nodeId
        }

        viewModelScope.launch(Dispatchers.IO + parentJob) {

            mindMapRepository.deleteEdgeEntitiesByNodeId(nodeId)

            launch(Dispatchers.Main) {
                edgeEntities.removeAll(predicate)
                _edgeEntityStates.removeAll(predicate)
            }
        }
    }

    private fun swapNode(index1: Int, index2: Int) {
        val tmp1 = nodeEntities[index1]
        val tmp2 = nodeEntities[index2]
        val tmp3 = _nodeEntityStates[index1]
        val tmp4 = _nodeEntityStates[index2]
        val tmp5 = _dataEntityStates[index1]
        val tmp6 = _dataEntityStates[index2]

        nodeEntities[index1] = tmp2
        nodeEntities[index2] = tmp1
        _nodeEntityStates[index1] = tmp4
        _nodeEntityStates[index2] = tmp3
        _dataEntityStates[index1] = tmp6
        _dataEntityStates[index2] = tmp5
    }

    /***/

    /**
     * @Test
     * Long Press ( multiple gesture detecting )
     * */

    private val _targetForLongPress = mutableStateOf<NodeEntity?>(null)
    val targetForLongPress: State<NodeEntity?> = _targetForLongPress

    private val _deleteMenu = mutableStateOf<Offset?>(null)
    val deleteMenu: State<Offset?> = _deleteMenu

    private val _moveMenu = mutableStateOf<Offset?>(null)
    val moveMenu: State<Offset?> = _moveMenu

    private val _editMenu = mutableStateOf<Offset?>(null)
    val editMenu: State<Offset?> = _editMenu

    private var selectedMenu: Int? = null

    fun onDragStartAfterLongPress(index: Int) = viewModelScope.launch {

        val nodeEntity = _nodeEntityStates[index]

        drawJob?.cancelAndJoin()
        _targetForLongPress.value = nodeEntity

        val x = nodeEntity.x.toFloat()
        val y = nodeEntity.y.toFloat()
        val tmp = cos(PI / 6f).toFloat()

        _deleteMenu.value = Offset(x, y - DISTANCE_LP)
        _moveMenu.value = Offset(
            x = x - DISTANCE_LP * tmp,
            y = y + DISTANCE_LP / 2
        )
        _editMenu.value = Offset(
            x = x + DISTANCE_LP * tmp,
            y = y + DISTANCE_LP / 2
        )
    }

    val onDragEndAfterLongPress: () -> Unit = {
        viewModelScope.launch {
            val tmp = launch {
                when (selectedMenu) {
                    DELETE_MENU -> {
                        Log.e("URGENT_TAG", "DELETE")
                        targetForLongPress.value?.let { target ->
                            deleteNode(target, currentCoroutineContext().job)
                        }
                    }

                    MOVE_MENU -> {
                        Log.e("URGENT_TAG", "MOVE")
                        targetForLongPress.value?.let { target ->
                            val linkList = linkBumperRepository.getLinkBumper().first().linkList.toMutableList()
                            linkList.add(_dataEntityStates[nodeId2Index[target.id]!!])
                            linkBumperRepository.updateLinkBumper(LinkBumper(linkList))
                            deleteNode(target, currentCoroutineContext().job)
                        }
                    }

                    EDIT_MENU -> {
                        Log.e("URGENT_TAG", "EDIT")
                        targetForLongPress.value?.let { target ->
                            deleteEdgesByNodeId(target.id, currentCoroutineContext().job)
                        }
                    }

                    else -> {

                    }
                }
            }

            Log.e("URGENT_TAG", "deleteEdgesByNodeId: 1234", )


            tmp.join()

            Log.e("URGENT_TAG", "deleteEdgesByNodeId: 1235", )


            _targetForLongPress.value = null
            _deleteMenu.value = null
            _moveMenu.value = null
            _editMenu.value = null
            selectedMenu = null

            draw()
        }
    }

    val onDragCancelAfterLongPress: () -> Unit = {

        _targetForLongPress.value = null
        _deleteMenu.value = null
        _moveMenu.value = null
        _editMenu.value = null
        draw()
    }

    fun onDragAfterLongPress(index: Int, offset: Offset) {

        val target = _nodeEntityStates[index]
        val x = target.x + offset.x
        val y = target.y + offset.y
        _nodeEntityStates[index] = target.copy(x = x, y = y)

        var tmp: Int? = null
        listOf(_deleteMenu.value, _moveMenu.value, _editMenu.value)
            .forEachIndexed { index, offset ->
                offset?.let {
                    if (isCollision(
                            x,
                            y,
                            it.x.toDouble(),
                            it.y.toDouble(),
                            COLLISION_THRESHOLD_LP
                        )
                    ) {
                        tmp = index
                        return@forEachIndexed
                    }
                }
            }
        selectedMenu = tmp
    }
}
