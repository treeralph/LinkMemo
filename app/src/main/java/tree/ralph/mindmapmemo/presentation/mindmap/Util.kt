package tree.ralph.mindmapmemo.presentation.mindmap

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.abs

@Composable
fun pixelToDp(px: Double): Dp {
    val density = LocalDensity.current.density.toDouble()
    return (px / density).dp
}

fun isCollision(
    x1: Double,
    y1: Double,
    x2: Double,
    y2: Double,
): Boolean = (abs(x1 - x2) < COLLISION_THRESHOLD && abs(y1 - y2) < COLLISION_THRESHOLD)

