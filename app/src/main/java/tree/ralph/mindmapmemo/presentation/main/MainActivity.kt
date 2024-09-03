package tree.ralph.mindmapmemo.presentation.main

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.lifecycle.ViewModelProvider
import dagger.hilt.android.AndroidEntryPoint
import tree.ralph.mindmapmemo.data.remote.isYoutubeUrl
import tree.ralph.mindmapmemo.data.remote.openLinkIntent
import tree.ralph.mindmapmemo.data.remote.openYoutubeIntent
import tree.ralph.mindmapmemo.presentation.home.HomeViewModel
import tree.ralph.mindmapmemo.presentation.utils.MindMapMemoApp
import tree.ralph.mindmapmemo.ui.theme.MindMapMemoTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    companion object {init { System.loadLibrary("mindmapmemo") } }
    private lateinit var viewModel: HomeViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProvider(this)[HomeViewModel::class.java]

        setContent {
            MindMapMemoTheme {
                MindMapMemoApp(
                    openLinkIntent = linkIntent
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)

        when(intent.action) {
            Intent.ACTION_SEND -> {
                if(intent.type == "text/plain") {
                    intent.getStringExtra(Intent.EXTRA_TEXT)?.let {
                        viewModel.addLink(it)
                    }
                }
            }
        }
    }

    private val linkIntent: (String) -> Unit = { link ->
        startActivity(
            if (isYoutubeUrl(link)) openYoutubeIntent(link)
            else openLinkIntent(link)
        )
    }
}
