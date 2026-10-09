package com.dangeloretis.tareoapp.ui.screens.home.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dangeloretis.tareoapp.domain.model.Site
import com.dangeloretis.tareoapp.domain.repository.SiteRepository
import com.dangeloretis.tareoapp.domain.usecase.DeleteSiteUseCase
import com.dangeloretis.tareoapp.domain.usecase.SaveSiteUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class AdminSitesViewModel @Inject constructor(
    siteRepository: SiteRepository,
    private val saveSiteUseCase: SaveSiteUseCase,
    private val deleteSiteUseCase: DeleteSiteUseCase
) : ViewModel() {

    val sites: StateFlow<List<Site>> = siteRepository.getAllSitesFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    fun toggleSiteActiveStatus(site: Site) {
        viewModelScope.launch {
            val updatedSite = site.copy(isActive = !site.isActive)
            try {
                saveSiteUseCase(updatedSite)
            } catch (e: IllegalArgumentException) {
                _errorMessage.update { e.message }
            }
        }
    }

    fun saveSite(site: Site) {
        viewModelScope.launch {
            val siteToSave = if (site.id.isEmpty()) {
                site.copy(id = UUID.randomUUID().toString())
            } else {
                site
            }
            try {
                saveSiteUseCase(siteToSave)
            } catch (e: IllegalArgumentException) {
                _errorMessage.update { e.message }
            }
        }
    }

    fun deleteSite(siteId: String) {
        viewModelScope.launch {
            try {
                deleteSiteUseCase(siteId)
            } catch (e: IllegalArgumentException) {
                _errorMessage.update { e.message }
            }
        }
    }

    fun clearError() {
        _errorMessage.update { null }
    }
}
