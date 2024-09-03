package tree.ralph.mindmapmemo.presentation.utils

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import tree.ralph.mindmapmemo.presentation.home.HomeScreen
import tree.ralph.mindmapmemo.presentation.mindmap.MindMapScreen

@Composable
fun MindMapMemoApp(
    openLinkIntent: (String) -> Unit = {},
) {
    val navController = rememberNavController()
    MindMapMemoNavHost(
        navController = navController,
        openLinkIntent = openLinkIntent
    )
}

@Composable
fun MindMapMemoNavHost(
    openLinkIntent: (String) -> Unit = {},
    navController: NavHostController
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(route = Screen.Home.route) {
            HomeScreen(navController = navController)
        }
        composable(route = Screen.MindMap.route) {
            MindMapScreen(
                navController = navController,
                linkIntent = openLinkIntent
            )
        }
    }
}