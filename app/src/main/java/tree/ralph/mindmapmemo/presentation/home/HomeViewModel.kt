package tree.ralph.mindmapmemo.presentation.home

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import tree.ralph.mindmapmemo.data.local.model.Folder
import tree.ralph.mindmapmemo.data.repository.MindMapRepository
import javax.inject.Inject

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
    private val mindMapRepository: MindMapRepository
): ViewModel() {

    private val _folders = MutableStateFlow<List<Folder>>(listOf())
    val folders = _folders.asStateFlow()

    private val _isDialogState = mutableStateOf(false)
    val isDialogState: State<Boolean> = _isDialogState

    private val _addFolderDialogUiState = mutableStateOf(AddFolderDialogUiState())
    val addFolderDialogUiState: State<AddFolderDialogUiState> = _addFolderDialogUiState

    private val _isAddNodeDialogState = mutableStateOf(false)
    val isAddNodeDialogState: State<Boolean> = _isAddNodeDialogState

    private val _addNodeDialogUiState = mutableStateOf(AddNodeDialogUiState())
    val addNodeDialogUiState : State<AddNodeDialogUiState> = _addNodeDialogUiState

    init {
        viewModelScope.launch {
            mindMapRepository.getAllFolders().collect {
                _folders.emit(it)
            }
        }
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

    fun observeIntentLink(link: String?) {
        link?.let {
            _addNodeDialogUiState.value = _addNodeDialogUiState.value.copy(link = it)
            _isAddNodeDialogState.value = true
        }
    }
}

