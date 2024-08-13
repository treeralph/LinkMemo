package tree.ralph.mindmapmemo.presentation.home

import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import tree.ralph.mindmapmemo.data.local.model.DataEntity
import tree.ralph.mindmapmemo.data.local.model.Folder
import tree.ralph.mindmapmemo.data.local.model.LinkBumper
import tree.ralph.mindmapmemo.data.local.model.NodeEntity
import tree.ralph.mindmapmemo.data.repository.LinkBumperRepository
import tree.ralph.mindmapmemo.data.repository.MindMapRepository
import tree.ralph.mindmapmemo.data.repository.OpenProtocolRepository
import javax.inject.Inject
import kotlin.random.Random

data class AddFolderDialogUiState(
    val content: String = "",
    val isError: Boolean = false,
    val errorMessage: String = ""
)

data class AddNodeDialogUiState(
    val link: String = "",
    val folder: Folder = Folder()
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val mindMapRepository: MindMapRepository,
    private val linkBumperRepository: LinkBumperRepository,
    private val openProtocolRepository: OpenProtocolRepository
): ViewModel() {

    private val _test = MutableStateFlow("")
    val test = _test.asStateFlow()

    fun onTestChanged(change: String) {
        _test.value = change
    }

    private val _folders = MutableStateFlow<List<Folder>>(listOf())
    val folders = _folders.asStateFlow()

    private val _isDialogState = mutableStateOf(false)
    val isDialogState: State<Boolean> = _isDialogState

    private val _addFolderDialogUiState = mutableStateOf(AddFolderDialogUiState())
    val addFolderDialogUiState: State<AddFolderDialogUiState> = _addFolderDialogUiState

    private val _sharedLinks = MutableStateFlow<List<DataEntity>>(listOf())
    val sharedLinks = _sharedLinks.asStateFlow()

    private val _isAddNodeDialogState = mutableStateOf(false)
    val isAddNodeDialogState: State<Boolean> = _isAddNodeDialogState

    private val _addNodeDialogUiState = mutableStateOf(AddNodeDialogUiState())
    val addNodeDialogUiState : State<AddNodeDialogUiState> = _addNodeDialogUiState

    private val _isSharedLinkDialogState = mutableStateOf(false)
    val isSharedLinkDialogState: State<Boolean> = _isSharedLinkDialogState

    init {
        viewModelScope.launch {
            launch {
                mindMapRepository.getAllFolders().collect {
                    _folders.emit(it)
                }
            }

            launch {
                linkBumperRepository.getLinkBumper().collectLatest {
                    _sharedLinks.emit(it.linkList)
                }
            }
        }
    }

    fun openSharedLinkDialog() {
        _isSharedLinkDialogState.value = true
    }

    fun closeSharedLinkDialog() {
        _isSharedLinkDialogState.value = false
    }

    fun openDialog() {
        _isDialogState.value = true

    }

    fun closeDialog() {
        _addFolderDialogUiState.value = AddFolderDialogUiState()
        _isDialogState.value = false
    }

    fun onChangeCurrentFolder(folder: Folder) {
        mindMapRepository.onChangeCurrentFolder(folder)
    }

    fun insertFolder() {
        viewModelScope.launch(Dispatchers.IO) {
            if(!isErrorFolderName()) {
                _addFolderDialogUiState.value.let {
                    mindMapRepository.insertFolder(
                        folderName = it.content,
                        folderInfo = "",
                    )
                }
                closeDialog()
            }
        }
    }

    private suspend fun isErrorFolderName(): Boolean {
        var folderName = _addFolderDialogUiState.value.content
        folderName = folderName.trim()

        if(folderName.isEmpty()) {
            _addFolderDialogUiState.value = _addFolderDialogUiState.value.copy(
                isError = true,
                errorMessage = BLANK_FOLDER_NAME_ERR_MSG
            )
            return true
        }

        val temp = mindMapRepository.getFoldersByName(folderName)
        if(temp.isNotEmpty()) {
            _addFolderDialogUiState.value = _addFolderDialogUiState.value.copy(
                isError = true,
                errorMessage = ALREADY_EXIST_FOLDER_NAME_ERR_MSG
            )
            return true
        }

        return false
    }

    fun onDialogUiStateChanged(new: String) {
        _addFolderDialogUiState.value = _addFolderDialogUiState.value.copy(content = new)
    }

    fun addLink(link: String) {
        val temp = _sharedLinks.value.toMutableList()
        if(temp.firstOrNull { it.linkUrl == link  } == null) {
            viewModelScope.launch(Dispatchers.IO) {
                val response = openProtocolRepository.getResponse(link)
                temp.add(
                    DataEntity(
                        imgUri = response.imageUrl,
                        linkUrl = link,
                        content = response.title,
                        description = response.description,
                    )
                )
                updateBumperLink(temp)
            }
        }
    }

    private fun updateBumperLink(linkList: List<DataEntity>) {
        viewModelScope.launch {
            linkBumperRepository.updateLinkBumper(
                LinkBumper(linkList)
            )
        }
    }

    fun insertNodeToFolder(folder: Folder, dataEntity: DataEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            val nodeEntity = NodeEntity(
                x = Random.nextDouble(-30.0, 30.0),
                y = Random.nextDouble(-30.0, 30.0),
            )

            mindMapRepository.insertNode(nodeEntity, dataEntity, folder.id)

            val temp = _sharedLinks.value.toMutableList()
            temp.remove(dataEntity)
            _sharedLinks.emit(temp)
            updateBumperLink(temp)
        }
    }
}
