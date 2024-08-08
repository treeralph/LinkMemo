package tree.ralph.mindmapmemo.data.remote

import android.content.Intent
import android.net.Uri
import android.util.Log

fun isYoutubeUrl(link: String): Boolean {
    val pattern = "^(http(s)?:\\/\\/)?((w){3}.)?youtu(be|.be)?(\\.com)?\\/.+"
    return !link.isEmpty() && link.matches(pattern.toRegex())
}

fun openYoutubeIntent(youtubeURI: String): Intent {
    return Intent(Intent.ACTION_VIEW).apply {
        data = Uri.parse(youtubeURI)
        setPackage("com.google.android.youtube")
    }
}

fun openLinkIntent(link: String): Intent {
    Log.e("TAG", "")
    return Intent(Intent.ACTION_VIEW, Uri.parse(link))
}

fun isNaver(link: String): Boolean = link.startsWith("https://naver.me/")
