package tree.ralph.mindmapmemo.presentation.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import tree.ralph.mindmapmemo.data.local.model.LinkBumper
import tree.ralph.mindmapmemo.data.repository.LinkBumperRepository
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val linkBumperRepository: LinkBumperRepository
): ViewModel() {

    private val _sharedLinks = MutableStateFlow(listOf<String>())
    val sharedLinks = _sharedLinks.asStateFlow()

    init {
        viewModelScope.launch {
            linkBumperRepository.getLinkBumper().collectLatest {
                _sharedLinks.emit(it.linkList)
            }
        }
    }

    fun addLink(link: String) {
        val temp = _sharedLinks.value.toMutableList()
        if(!temp.contains(link)) {
            temp.add(link)
            updateBumperLink(temp)
        }
    }

    private fun updateBumperLink(linkList: List<String>) {
        viewModelScope.launch {
            linkBumperRepository.updateLinkBumper(
                LinkBumper(linkList)
            )
        }
    }
}