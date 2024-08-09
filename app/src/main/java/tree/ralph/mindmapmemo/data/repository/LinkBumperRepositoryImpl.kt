package tree.ralph.mindmapmemo.data.repository

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import tree.ralph.mindmapmemo.data.local.model.LinkBumper
import javax.inject.Inject

private val Context.dataStore by preferencesDataStore(name = LINK_BUMPER_DATASTORE)

class LinkBumperRepositoryImpl @Inject constructor(
    private val context: Context
): LinkBumperRepository {

    private val gson = Gson()
    private val key = stringPreferencesKey(LINK_BUMPER_KEY)

    override fun getLinkBumper(): Flow<LinkBumper> = context.dataStore.data
        .catch {
            LinkBumper(listOf())
        }.map { preferences ->
            val temp = preferences[key] ?: ""
            if(temp.isEmpty()) { LinkBumper(listOf()) }
            else { gson.fromJson(temp, LinkBumper::class.java) }
        }

    override suspend fun updateLinkBumper(linkBumper: LinkBumper) {
        context.dataStore.edit {
            it[key] = gson.toJson(linkBumper)
        }
    }
}
