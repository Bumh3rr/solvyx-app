package com.solvyx.ui.screens.directory

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.solvyx.ui.screens.directory.data.DirectoryData
import com.solvyx.ui.screens.directory.model.DirectoryCategory
import com.solvyx.ui.screens.directory.model.DirectoryEntry
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class DirectoryViewModel @Inject constructor(
    @ApplicationContext appContext: Context
) : ViewModel() {

    var filter by mutableStateOf(DirectoryFilter())
        private set

    val results: List<DirectoryEntry> by derivedStateOf { DirectoryData.entries.filterBy(filter) }

    /** Lines that answer right now, whatever the filter — shown as "Hablar ahora". */
    val availableNow: List<DirectoryEntry> = DirectoryData.entries.filter { it.available24h }

    private val connectivityManager = appContext.getSystemService(ConnectivityManager::class.java)
    private val _isOnline = MutableStateFlow(isCurrentlyOnline())

    /** Live connectivity: directions need internet, calls don't. */
    val isOnline: StateFlow<Boolean> = _isOnline.asStateFlow()

    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) { _isOnline.value = true }
        override fun onLost(network: Network) { _isOnline.value = isCurrentlyOnline() }
    }

    init {
        runCatching { connectivityManager?.registerDefaultNetworkCallback(networkCallback) }
    }

    fun onQueryChange(query: String) {
        filter = filter.copy(query = query)
    }

    fun onCategoryChange(category: DirectoryCategory?) {
        filter = filter.copy(category = category)
    }

    fun onToggleOnlyFree() {
        filter = filter.copy(onlyFree = !filter.onlyFree)
    }

    fun clearFilters() {
        filter = DirectoryFilter()
    }

    private fun isCurrentlyOnline(): Boolean = runCatching {
        val capabilities = connectivityManager?.getNetworkCapabilities(connectivityManager.activeNetwork)
        capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
    }.getOrDefault(false)

    override fun onCleared() {
        runCatching { connectivityManager?.unregisterNetworkCallback(networkCallback) }
    }
}
