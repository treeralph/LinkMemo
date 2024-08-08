package tree.ralph.mindmapmemo.presentation

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import dagger.hilt.android.AndroidEntryPoint
import tree.ralph.mindmapmemo.presentation.utils.MindMapMemoApp
import tree.ralph.mindmapmemo.ui.theme.MindMapMemoTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    companion object {init { System.loadLibrary("mindmapmemo") } }
    private var sharedLink: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MindMapMemoTheme {
                MindMapMemoApp()
            }
        }

        when(intent.action) {
            Intent.ACTION_SEND -> {
                if(intent.type == "text/plain") {
                    intent.getStringExtra(Intent.EXTRA_TEXT)?.let {
                        sharedLink = it
                    }
                }
            }
        }
    }

    fun getSharedLink(): String? = sharedLink
}
