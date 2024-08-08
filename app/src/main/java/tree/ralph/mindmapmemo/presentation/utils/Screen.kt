package tree.ralph.mindmapmemo.presentation.utils

sealed class Screen(
    val route: String
) {
    data object Home: Screen(HOME)
    data object MindMap: Screen(MIND_MAP_SCREEN)
}