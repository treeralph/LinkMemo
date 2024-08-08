package tree.ralph.mindmapmemo.data.remote

import org.jsoup.Jsoup
import tree.ralph.mindmapmemo.data.remote.model.OpenProtocolResponse
import java.util.regex.Pattern
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class OpenProtocolDataSource {
    suspend fun getResponse(link: String): OpenProtocolResponse {
        return suspendCoroutine {
            val result = OpenProtocolResponse()
            if (link.isNotEmpty()) {
                var linkBumper = link

                /** NAVER APP LINK 전처리 */
                if(isNaver(link)) {
                    // Link from NAVER app
                    val doc = Jsoup.connect(link).get()
                    val metaTags = doc.select("meta")
                    for (metaTag in metaTags) {
                        if (metaTag.attr("property") == "al:android:url") {
                            var content = metaTag.attr("content")
                            content = content.replace("%3A", ":")
                            content = content.replace("%2F", "/")
                            content = content.replace("%3D", "=")
                            content = content.replace("%3F", "?")
                            content = content.replace("%26", "&")
                            val regex = "\\?url=([^&]*)&"
                            val pattern = Pattern.compile(regex)
                            val matcher = pattern.matcher(content)
                            if (matcher.find()) {
                                linkBumper = matcher.group(1)
                            }
                        }
                    }
                }

                val doc = Jsoup.connect(linkBumper).get()
                val metaTags = doc.select("meta[property^=og:]")
                var title: String? = null
                var description: String? = null
                var imageUrl: String? = null
                for (metaTag in metaTags) {
                    val property = metaTag.attr("property")
                    val content = metaTag.attr("content")
                    when (property) {
                        "og:title" -> title = content
                        "og:description" -> description = content
                        "og:image" -> imageUrl = content
                    }
                }
                result.title = title ?: ""
                result.description = description ?: ""
                result.imageUrl = imageUrl ?: ""
            }

            it.resume(result)
        }
    }
}
