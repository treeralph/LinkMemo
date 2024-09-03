package tree.ralph.mindmapmemo.presentation.mindmap

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import tree.ralph.mindmapmemo.data.local.model.DataEntity

@Composable
fun NodeDetailDialog(
    visible: Boolean,
    onDismissRequest: () -> Unit,
    dataEntity: DataEntity?,
    editButtonClickListener: () -> Unit,
    goToLinkButtonClickListener: () -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(animationSpec = tween(durationMillis = 1000)) { it } +
                fadeIn(
                    initialAlpha = 0f,
                    animationSpec = tween(durationMillis = 1000)
                ),
        exit = slideOutVertically { it } + fadeOut()
    ) {
        Dialog(onDismissRequest = onDismissRequest) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentSize(),
                shape = RoundedCornerShape(36.dp),
            ) {
                NodeDetailDialogHead(
                    modifier = Modifier.fillMaxWidth(),
                    editButtonClickListener = editButtonClickListener,
                    goToLinkButtonClickListener = goToLinkButtonClickListener
                )
                NodeDetailDialogBody(
                    modifier = Modifier.fillMaxWidth(),
                    dataEntity = dataEntity?: DataEntity()
                )
            }
        }
    }
}

@Composable
private fun NodeDetailDialogHead(
    modifier: Modifier,
    editButtonClickListener: () -> Unit,
    goToLinkButtonClickListener: () -> Unit,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        Spacer(modifier = Modifier.weight(1f))

        /** Edit Button */
        IconButton(
            onClick = editButtonClickListener,
        ) {
            Icon(
                modifier = Modifier.padding(8.dp),
                imageVector = Icons.Outlined.Settings,
                contentDescription = "",
            )
        }


        /** goToLink Button */
        IconButton(
            onClick = goToLinkButtonClickListener
        ) {
            Icon(
                modifier = Modifier.padding(8.dp),
                imageVector = Icons.Outlined.Link,
                contentDescription = ""
            )
        }
        Spacer(modifier = Modifier.size(12.dp))
    }
}

@Composable
private fun NodeDetailDialogBody(
    modifier: Modifier,
    dataEntity: DataEntity,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
    ) {
        /** Image */
        Box(modifier = modifier) {
            if (dataEntity.imgUri.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                ) {
                    Text(
                        modifier = Modifier.align(Alignment.Center),
                        text = dataEntity.content
                    )
                }
            } else {
                AsyncImage(
                    modifier = Modifier.fillMaxWidth(),
                    model = dataEntity.imgUri,
                    contentDescription = "",
                    contentScale = ContentScale.FillWidth,
                )
            }
        }

        /** Content */
        Box(modifier = Modifier.fillMaxWidth()) {
            Text(
                modifier = Modifier.padding(8.dp),
                text = dataEntity.content,
            )
        }

        /** Description */
        Box(modifier = Modifier.fillMaxWidth()) {
            Text(
                modifier = Modifier.padding(
                    start = 8.dp,
                    end = 8.dp,
                    bottom = 32.dp
                ),
                text = dataEntity.description,
            )
        }
    }
}