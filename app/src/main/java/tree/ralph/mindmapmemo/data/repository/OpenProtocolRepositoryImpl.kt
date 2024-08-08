package tree.ralph.mindmapmemo.data.repository

import tree.ralph.mindmapmemo.data.remote.OpenProtocolDataSource
import tree.ralph.mindmapmemo.data.remote.model.OpenProtocolResponse
import java.lang.Exception
import javax.inject.Inject

class OpenProtocolRepositoryImpl @Inject constructor(
    private val openProtocolDataSource: OpenProtocolDataSource
): OpenProtocolRepository {
    override suspend fun getResponse(
        link: String
    ): OpenProtocolResponse {
        return try {
            openProtocolDataSource.getResponse(link)
        } catch(e: Exception) {
            OpenProtocolResponse(title = link)
        }
    }
}
