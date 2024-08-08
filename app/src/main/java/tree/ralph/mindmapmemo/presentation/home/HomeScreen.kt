package tree.ralph.mindmapmemo.presentation.home

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import tree.ralph.mindmapmemo.R
import tree.ralph.mindmapmemo.data.local.model.Folder
import tree.ralph.mindmapmemo.presentation.MainActivity
import tree.ralph.mindmapmemo.presentation.utils.Screen

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    navController: NavHostController,
) {
    val folders by viewModel.folders.collectAsState()

    viewModel.observeIntentLink((LocalContext.current as MainActivity).getSharedLink())

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { viewModel.openDialog() }) {
                Icon(imageVector = Icons.Filled.Add, contentDescription = "")
            }
        }
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(modifier = Modifier.padding(it)) {
                LazyColumn {
                    items(
                        items = folders,
                        key = { it.id }
                    ) {
                        FolderComposable(
                            folder = it,
                            onFolderClickListener = {
                                viewModel.onChangeCurrentFolder(it)
                                navController.navigate(route = Screen.MindMap.route)
                            }
                        )
                    }
                }
            }
        }


        if (viewModel.isDialogState.value) {
            AddFolderDialog(
                addFolderDialogUiState = viewModel.addFolderDialogUiState.value,
                onTitleChanged = { viewModel.onDialogUiStateChanged(it) },
                onDismiss = {
                    viewModel.closeDialog()
                },
                onConfirm = {
                    viewModel.insertFolder()
                }
            )
        }
    }
}

@Composable
fun FolderComposable(
    folder: Folder,
    onFolderClickListener: () -> Unit,
) {
    Button(
        onClick = onFolderClickListener
    ) {
        Text(text = folder.folderName)
    }
}

@Composable
fun AddFolderDialog(
    addFolderDialogUiState: AddFolderDialogUiState,
    onTitleChanged: (String) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(imageVector = Icons.Filled.Folder, contentDescription = "") },
        title = {
            Text(text = stringResource(id = R.string.add_folder_dialog_description))  
        },
        text = {
            OutlinedTextField(
                value = addFolderDialogUiState.content,
                onValueChange = onTitleChanged,
                isError = addFolderDialogUiState.isError,
                supportingText = {
                    if(addFolderDialogUiState.isError) {
                        Text(text = addFolderDialogUiState.errorMessage)
                    }
                }
            )
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "DISMISS")
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(text = "CONFIRM")
            }
        },
    )
}


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AddNodeDialog(
    addNodeDialogUiState: AddNodeDialogUiState,
    folders: List<Folder>,
    onDismiss: () -> Unit,
    onConfirm: (Folder) -> Unit,
) {
    val pagerState = rememberPagerState { folders.size }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(imageVector = Icons.Filled.AddCircle, contentDescription = "") },
        title = { Text(text = stringResource(id = R.string.add_node_dialog_with_folder_description)) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(text = addNodeDialogUiState.link)
                HorizontalPager(state = pagerState) {
                    Text(text = folders[it].folderName)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "DISMISS")
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(folders[pagerState.currentPage]) }) {
                Text(text = "CONFIRM")
            }
        },
    )

}
