package tree.ralph.mindmapmemo.presentation.mindmap

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddBox
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoveToInbox
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import tree.ralph.mindmapmemo.R
import tree.ralph.mindmapmemo.data.local.model.Folder
import tree.ralph.mindmapmemo.data.local.model.NODE_RADIUS
import kotlin.math.min

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
            key(edgeEntityState.id) {

                /**
                 * @Composable
                 * fun EdgeComposable
                 * */

                val start = viewModel.getNodeEntity(edgeEntityState.node1)
                val end = viewModel.getNodeEntity(edgeEntityState.node2)

                Box(
                    modifier = Modifier
                        .offset {
                            IntOffset(
                                x = min(start.x, end.x).toInt(),
                                y = min(start.y, end.y).toInt()
                            )
                        }
                        .size(width = 1.dp, height = 1.dp)
                ) {
                    Spacer(
                        modifier = Modifier
                            .drawBehind {
                                drawLine(
                                    color = Color.Black,
                                    start = Offset(
                                        (start.x - min(start.x, end.x)).toFloat(),
                                        (start.y - min(start.y, end.y)).toFloat()
                                    ),
                                    end = Offset(
                                        (end.x - min(start.x, end.x)).toFloat(),
                                        (end.y - min(start.y, end.y)).toFloat()
                                    ),
                                    alpha = STROKE_ALPHA,
                                    strokeWidth = STROKE_WIDTH
                                )
                            }
                            .fillMaxSize()
                    )
                }

                /**
                 * /EdgeComposable
                 * */
            }
        }

        viewModel.nodeEntityStates.forEachIndexed { index, nodeEntityState ->
            key(nodeEntityState.id) {

                /**
                 * @Composable
                 * fun NodeComposable
                 * */

                Box(
                    modifier = Modifier
                        .offset {
                            IntOffset(
                                x = (nodeEntityState.x - NODE_RADIUS).toInt(),
                                y = (nodeEntityState.y - NODE_RADIUS).toInt()
                            )
                        }
                        .size(pixelToDp(px = NODE_RADIUS * 2))
                        .clickable { viewModel.openDetailNode(index) }
                        .pointerInput(Unit) {
                            detectDragGestures(
                                onDragStart = { viewModel.onNodeDragStart(nodeEntityState) },
                                onDragEnd = { viewModel.onNodeDragEnd(index) },
                                onDragCancel = { viewModel.onNodeDragEnd(index) }
                            ) { change, dragAmount ->
                                change.consume()
                                viewModel.onNodeMoved(index, dragAmount)
                            }
                        }
                        .pointerInput(Unit) {
                            detectDragGesturesAfterLongPress(
                                onDragStart = { viewModel.onDragStartAfterLongPress(index) },
                                onDragEnd = viewModel.onDragEndAfterLongPress,
                                onDragCancel = viewModel.onDragCancelAfterLongPress,
                                onDrag = { change, dragAmount ->
                                    change.consume()
                                    viewModel.onDragAfterLongPress(index, dragAmount)
                                }
                            )
                        }
                ) {
                    Spacer(
                        modifier = Modifier
                            .offset {
                                IntOffset(
                                    (NODE_RADIUS / 2).toInt(),
                                    (NODE_RADIUS / 2).toInt()
                                )
                            }
                            .size(pixelToDp(px = NODE_RADIUS))
                            .clip(CircleShape)
                            .background(color = MaterialTheme.colorScheme.onSecondary),
                    )

                    if(viewModel.getDataEntityState(index).imgUri.isNotEmpty()) {
                        SubNodeComposableWithImage(dataEntity = viewModel.getDataEntityState(index))
                    }else {
                        SubNodeComposable(dataEntity = viewModel.getDataEntityState(index))
                    }
                }

                /**
                 * /NodeComposable
                 * */
            }
        }

        viewModel.notificationNodeEntityState.value?.let {
            NotificationNode(
                node = { it }
            )
        }

        /**
         * @Test
         * Long Press ( multiple gesture detecting )
         * */

        viewModel.targetForLongPress.value?.let {
            Box(
                modifier = Modifier
                    .offset {
                        IntOffset(
                            it.x.toInt() - BACKGROUND_SIZE_LP / 2,
                            it.y.toInt() - BACKGROUND_SIZE_LP / 2
                        )
                    }
                    .size(pixelToDp(px = BACKGROUND_SIZE_LP.toDouble()))
                    .alpha(0.5f)
                    .background(
                        color = Color.Gray,
                        shape = CircleShape
                    )
            )

            viewModel.deleteMenu.value?.let {
                Box(
                    modifier = Modifier
                        .offset {
                            IntOffset(
                                it.x.toInt() - MENU_SIZE_LP / 2,
                                it.y.toInt() - MENU_SIZE_LP / 2
                            )
                        }
                        .size(pixelToDp(px = MENU_SIZE_LP.toDouble()))
                        .alpha(0.5f)
                        .background(
                            color = Color.Blue,
                            shape = CircleShape
                        )
                ) {
                    Icon(
                        modifier = Modifier.align(Alignment.Center),
                        imageVector = Icons.Filled.Delete,
                        contentDescription = ""
                    )
                }
            }

            viewModel.moveMenu.value?.let {
                Box(
                    modifier = Modifier
                        .offset {
                            IntOffset(
                                it.x.toInt() - MENU_SIZE_LP / 2,
                                it.y.toInt() - MENU_SIZE_LP / 2
                            )
                        }
                        .size(pixelToDp(px = MENU_SIZE_LP.toDouble()))
                        .alpha(0.5f)
                        .background(
                            color = Color.Blue,
                            shape = CircleShape
                        )
                ) {
                    Icon(
                        modifier = Modifier.align(Alignment.Center),
                        imageVector = Icons.Filled.MoveToInbox,
                        contentDescription = ""
                    )
                }
            }

            viewModel.editMenu.value?.let {
                Box(
                    modifier = Modifier
                        .offset {
                            IntOffset(
                                it.x.toInt() - MENU_SIZE_LP / 2,
                                it.y.toInt() - MENU_SIZE_LP / 2
                            )
                        }
                        .size(pixelToDp(px = MENU_SIZE_LP.toDouble()))
                        .alpha(0.5f)
                        .background(
                            color = Color.Blue,
                            shape = CircleShape
                        )
                ) {
                    Icon(
                        modifier = Modifier.align(Alignment.Center),
                        imageVector = Icons.Filled.Edit,
                        contentDescription = ""
                    )
                }
            }
        }

        /**
         * @Test - end
         * */

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
                                x = (1 - zoom) * (centroid.x - offset.x - screenWidth / 2),
                                y = (1 - zoom) * (centroid.y - offset.y - screenHeight / 2)
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