package tree.ralph.mindmapmemo.data.repository

import tree.ralph.mindmapmemo.data.remote.model.OpenProtocolResponse

interface OpenProtocolRepository {
    suspend fun getResponse(link: String): OpenProtocolResponse
}