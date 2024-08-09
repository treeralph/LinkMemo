package tree.ralph.mindmapmemo.data.repository

import kotlinx.coroutines.flow.Flow
import tree.ralph.mindmapmemo.data.local.model.LinkBumper

interface LinkBumperRepository {
    fun getLinkBumper(): Flow<LinkBumper>
    suspend fun updateLinkBumper(linkBumper: LinkBumper)
}