package tree.ralph.mindmapmemo.presentation.home

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.AddLink
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import tree.ralph.mindmapmemo.R
import tree.ralph.mindmapmemo.data.local.model.DataEntity
import tree.ralph.mindmapmemo.data.local.model.Folder
import tree.ralph.mindmapmemo.presentation.utils.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    navController: NavHostController,
) {
    val folders by viewModel.folders.collectAsState()

    Scaffold(
        topBar = {
            HomeScreenTopAppBar(
                numberNotification = viewModel
                    .sharedLinks.collectAsState(initial = listOf()).value.size,
                sharedLinkButtonClickListener = {
                    viewModel.openSharedLinkDialog()
                }
            )
        },
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

        if (viewModel.isSharedLinkDialogState.value) {
            LinkPreviewDialog(
                preview = viewModel.sharedLinks.collectAsState(initial = listOf()).value,
                folders = folders,
                onDismiss = { viewModel.closeSharedLinkDialog() },
                onSendButtonClickListener = { folder, dataEntity ->
                    viewModel.insertNodeToFolder(folder, dataEntity)
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreenTopAppBar(
    numberNotification: Int,
    sharedLinkButtonClickListener: () -> Unit,
) {
    TopAppBar(
        title = {},
        actions = {
            BadgedBox(
                badge = {
                    if(numberNotification != 0) {
                        Badge {
                            Text(text = "$numberNotification")
                        }
                    }
                }
            ) {
                Icon(
                    modifier = Modifier.clickable { sharedLinkButtonClickListener() },
                    imageVector = Icons.Filled.AddLink,
                    contentDescription = ""
                )
            }
            Spacer(modifier = Modifier.size(24.dp))
        }
    )
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
                    if (addFolderDialogUiState.isError) {
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LinkPreviewDialog(
    onSendButtonClickListener: (Folder, DataEntity) -> Unit,
    onDismiss: () -> Unit = {},
    preview: List<DataEntity>,
    folders: List<Folder>
) {
    val verticalPagerState = rememberPagerState { preview.size }
    val horizontalPagerState = rememberPagerState { folders.size }

    Dialog(onDismissRequest = onDismiss) {
        Card {
            if(preview.size != 0) {
                Column {
                    VerticalPager(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp),
                        state = verticalPagerState,
                        pageSpacing = 12.dp,
                        contentPadding = PaddingValues(
                            start = 0.dp,
                            end = 0.dp,
                            top = 0.dp,
                            bottom = 24.dp
                        )
                    ) {
                        Column {
                            AsyncImage(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp),
                                model = preview[it].imgUri,
                                contentDescription = "",
                                contentScale = ContentScale.Crop
                            )
                            Text(
                                modifier = Modifier.padding(12.dp),
                                text = preview[it].content,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                    HorizontalPager(state = horizontalPagerState) {
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                text = folders[it].folderName,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            onSendButtonClickListener(
                                folders[horizontalPagerState.currentPage],
                                preview[verticalPagerState.currentPage]
                            )
                        }
                    ) {
                        Text(text = "Send")
                    }
                }
            } else {
                Box(modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                ) {
                    Text(
                        modifier = Modifier.align(Alignment.Center),
                        text = stringResource(id = R.string.there_is_no_shared_link_description)
                    )
                }
            }
        }
    }
}
