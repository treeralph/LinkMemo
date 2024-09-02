package tree.ralph.mindmapmemo.presentation.mindmap

import android.content.res.Resources
import androidx.compose.ui.unit.dp

const val STROKE_WIDTH = 2f
const val STROKE_ALPHA = 0.7f
const val STROKE_BOX_SIZE_THRESHOLD = 6.0

const val COLLISION_THRESHOLD = 45

val screenWidth = Resources.getSystem().displayMetrics.widthPixels
val screenHeight = Resources.getSystem().displayMetrics.heightPixels

/**
 * @Test
 * Long Press ( multiple gesture detecting )
 * */

const val COLLISION_THRESHOLD_LP = 64
const val DISTANCE_LP = 196
const val MENU_SIZE_LP = 96
const val BACKGROUND_SIZE_LP = 256

const val DELETE_MENU = 0
const val MOVE_MENU = 1
const val EDIT_MENU = 2
const val ERROR_MENU = -1