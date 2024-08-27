package tree.ralph.mindmapmemo.presentation.mindmap

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import tree.ralph.mindmapmemo.R
import tree.ralph.mindmapmemo.data.local.model.DataEntity
import tree.ralph.mindmapmemo.data.local.model.NODE_RADIUS
import tree.ralph.mindmapmemo.data.local.model.NodeEntity
import tree.ralph.mindmapmemo.ui.theme.Pink40
import kotlin.math.min

@Composable
fun NodeComposable(
    nodeEntity: () -> NodeEntity,
    dataEntity: () -> DataEntity,
    dragAble: Boolean = true,
    onDragStart: () -> Unit = {},
    onDragEnd: () -> Unit = {},
    onNodeMoved: (Offset) -> Unit = { _ -> },
    onClickListener: (NodeEntity) -> Unit,
) {
    Box(
        modifier = Modifier
            .offset {
                IntOffset(
                    x = (nodeEntity().x - NODE_RADIUS).toInt(),
                    y = (nodeEntity().y - NODE_RADIUS).toInt()
                )
            }
            .size(pixelToDp(px = NODE_RADIUS * 2))
            .clickable { onClickListener(nodeEntity()) }
            .pointerInput(Unit) {
                if (dragAble) {
                    detectDragGestures(
                        onDragStart = { onDragStart() },
                        onDragEnd = { onDragEnd() },
                        onDragCancel = { onDragEnd() }
                    ) { change, dragAmount ->
                        change.consume()
                        onNodeMoved(dragAmount)
                    }
                }
            }
    ) {
        Spacer(
            modifier = Modifier
                .offset { IntOffset((NODE_RADIUS / 2).toInt(), (NODE_RADIUS / 2).toInt()) }
                .size(pixelToDp(px = NODE_RADIUS))
                .clip(CircleShape)
                .background(color = MaterialTheme.colorScheme.onSecondary),
        )

        if(dataEntity().imgUri.isNotEmpty()) {
            SubNodeComposableWithImage(dataEntity = dataEntity())
        }else {
            SubNodeComposable(dataEntity = dataEntity())
        }
    }
}

@Composable
fun SubNodeComposable(
    dataEntity: DataEntity
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = MaterialTheme.colorScheme.secondaryContainer)
            .border(BorderStroke(1.dp, Color.White)),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.weight(1f))
        Text(
            modifier = Modifier.padding(0.dp),
            text = dataEntity.content,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.primary,
            overflow = TextOverflow.Ellipsis,
            maxLines = 2,
            softWrap = true
        )
        Spacer(modifier = Modifier.weight(1f))
    }
}

@Composable
fun SubNodeComposableWithImage(
    dataEntity: DataEntity
) {
    AsyncImage(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color.White)
            .border(BorderStroke(1.dp, Color.White)),
        model = dataEntity.imgUri,
        contentScale = ContentScale.Crop,
        contentDescription = "",
        placeholder = painterResource(id = R.drawable.ic_launcher_background),
    )

    Text(
        modifier = Modifier
            .offset { IntOffset(0, (NODE_RADIUS * 2).toInt() + 8) }
            .size(width = pixelToDp(px = NODE_RADIUS * 2), height = 8.dp),
        text = dataEntity.content,
        color = Color.White,
        maxLines = 1,
        fontSize = 6.sp,
        textAlign = TextAlign.Center,
        overflow = TextOverflow.Ellipsis
    )
}

@Composable
fun EdgeComposable(
    start: () -> NodeEntity,
    end: () -> NodeEntity,
) {
    Box(
        modifier = Modifier
            .offset {
                IntOffset(
                    x = min(start().x, end().x).toInt(),
                    y = min(start().y, end().y).toInt()
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
                            (start().x - min(start().x, end().x)).toFloat(),
                            (start().y - min(start().y, end().y)).toFloat()
                        ),
                        end = Offset(
                            (end().x - min(start().x, end().x)).toFloat(),
                            (end().y - min(start().y, end().y)).toFloat()
                        ),
                        alpha = STROKE_ALPHA,
                        strokeWidth = STROKE_WIDTH
                    )
                }
                .fillMaxSize()
        )
    }
}

@Composable
fun NotificationNode(
    node: () -> NodeEntity,
) {
    Box(
        modifier = Modifier
            .offset {
                IntOffset(
                    x = (node().x - NODE_RADIUS).toInt(),
                    y = (node().y - NODE_RADIUS).toInt()
                ) - IntOffset(100, 100)
            }
            .size(pixelToDp(px = (NODE_RADIUS + 100) * 2))
            .alpha(0.3f)
            .drawBehind {
                drawCircle(color = Pink40)
            }
    )
}
