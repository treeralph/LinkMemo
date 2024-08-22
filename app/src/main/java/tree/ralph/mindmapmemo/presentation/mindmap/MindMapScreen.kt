package tree.ralph.mindmapmemo.presentation.mindmap

import android.content.res.Resources
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddBox
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import tree.ralph.mindmapmemo.R
import tree.ralph.mindmapmemo.data.local.model.DataEntity
import tree.ralph.mindmapmemo.data.local.model.Folder

@Composable
fun MindMapScreen(
    viewModel: MindMapViewModel = hiltViewModel(),
    navController: NavHostController,
) {
    MindMapScreenScaffold(
        topBar = {
            MindMapTopAppBar(
                title = viewModel
                    .currentFolder
                    .collectAsState(initial = Folder())
                    .value!!
                    .folderName
            )
        },
        floatingActionButton = {
            MindMapAddNodeButton {
                viewModel.openAddNodeDialog()
            }
        }
    ) {
        viewModel.edgeEntityStates.forEach { edgeEntityState ->
            key(edgeEntityState.value.id) {
                EdgeComposable(
                    start = { viewModel.getNodeEntity(edgeEntityState.value.node1) },
                    end = { viewModel.getNodeEntity(edgeEntityState.value.node2) }
                )
            }
        }

        viewModel.nodeEntityStates.forEachIndexed { index, nodeEntityState ->
            key(nodeEntityState.value.id) {
                NodeComposable(
                    nodeEntity = { nodeEntityState.value },
                    dataEntity = { viewModel.getDataEntityState(index).value },
                    onDragStart = { viewModel.onNodeDragStart(nodeEntityState.value) },
                    onNodeMoved = { viewModel.onNodeMoved(index, it) },
                    onDragEnd = { viewModel.onNodeDragEnd(index) },
                    onClickListener = {

                    }
                )
            }
        }

        viewModel.notificationNodeEntityState.value?.let {
            NotificationNode(
                node = { it }
            )
        }

        if (viewModel.isAddNodeDialog.value) {
            AddNodeDialog(
                dialogUiState = viewModel.addNodeDialogUiState.value,
                onTitleChanged = { viewModel.onTitleChanged(it) },
                onDismiss = { viewModel.closeAddNodeDialog() },
                onConfirm = { viewModel.addNode() }
            )
        }

        NodeDetailDialog(
            visible = viewModel.isNodeDetailDialog.value,
            onDismissRequest = { viewModel.releaseDetailNode() },
            dataEntity = viewModel.currentDetailNode.value,
            deleteButtonClickListener = { /*TODO*/ },
            editButtonClickListener = { /*TODO*/ },
            goToLinkButtonClickListener = { /*TODO*/ }
        )
    }
}

@Composable
fun MindMapScreenScaffold(
    topBar: @Composable () -> Unit = {},
    bottomBar: @Composable () -> Unit = {},
    snackbarHost: @Composable () -> Unit = {},
    floatingActionButton: @Composable () -> Unit = {},
    floatingActionButtonPosition: FabPosition = FabPosition.End,
    containerColor: Color = MaterialTheme.colorScheme.background,
    contentColor: Color = contentColorFor(containerColor),
    contentWindowInsets: WindowInsets = ScaffoldDefaults.contentWindowInsets,
    content: @Composable BoxScope.() -> Unit,
) {
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    val width = Resources.getSystem().displayMetrics.widthPixels / 2f
    val height = Resources.getSystem().displayMetrics.heightPixels / 2f

    Scaffold(
        topBar = topBar,
        bottomBar = bottomBar,
        snackbarHost = snackbarHost,
        floatingActionButton = floatingActionButton,
        floatingActionButtonPosition = floatingActionButtonPosition,
        containerColor = containerColor,
        contentColor = contentColor,
        contentWindowInsets = contentWindowInsets,

        ) {
        Box(
            modifier = Modifier
                .padding(it)
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTransformGestures { centroid, pan, zoom, _ ->
                        if (zoom != 1f) {
                            scale *= zoom
                            offset += Offset(
                                x = (1 - zoom) * (centroid.x - offset.x - width),
                                y = (1 - zoom) * (centroid.y - offset.y - height)
                            )
                        } else offset += pan
                    }
                }
                .graphicsLayer(
                    scaleX = scale,
                    scaleY = scale,
                    translationX = offset.x,
                    translationY = offset.y
                )
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                content()
            }
        }
    }
}

@Composable
fun AddNodeDialog(
    dialogUiState: DialogUiState,
    onTitleChanged: (String) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(imageVector = Icons.Filled.AddBox, contentDescription = "") },
        title = {
            Text(text = stringResource(id = R.string.add_node_dialog_description))
        },
        text = {
            OutlinedTextField(
                value = dialogUiState.content,
                onValueChange = onTitleChanged,
                isError = dialogUiState.isError,
                supportingText = {
                    if (dialogUiState.isError) {
                        Text(text = dialogUiState.errorMessage)
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MindMapTopAppBar(
    title: String,
) {
    TopAppBar(
        title = { Text(text = title) }
    )
}

@Composable
fun MindMapAddNodeButton(
    onClick: () -> Unit,
) {
    FloatingActionButton(onClick = onClick) {
        Icon(imageVector = Icons.Filled.Add, contentDescription = "")
    }
}