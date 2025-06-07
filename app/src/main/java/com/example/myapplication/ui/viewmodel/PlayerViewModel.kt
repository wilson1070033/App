package com.example.myapplication.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.model.Player
import com.example.myapplication.data.model.StatSplit
import com.example.myapplication.data.repository.MlbRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar

data class PlayerUiState(
    val isSearching: Boolean = false,
    val searchResults: List<Player> = emptyList(),
    val selectedPlayer: Player? = null,
    val isLoadingStats: Boolean = false,
    val playerStats: List<StatSplit> = emptyList(),
    val currentStatGroup: String = "hitting",
    val currentStatType: String = "season",
    val currentSeason: String = Calendar.getInstance().get(Calendar.YEAR).toString(),
    val error: String? = null,
    val searchQuery: String = ""
)

class PlayerViewModel : ViewModel() {
    
    private val repository = MlbRepository()
    
    private val _uiState = MutableStateFlow(PlayerUiState())
    val uiState: StateFlow<PlayerUiState> = _uiState.asStateFlow()
    
    fun searchPlayers(query: String) {
        if (query.isBlank()) {
            _uiState.value = _uiState.value.copy(
                searchResults = emptyList(),
                searchQuery = query
            )
            return
        }
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isSearching = true,
                error = null,
                searchQuery = query
            )
            
            repository.searchPlayers(query)
                .onSuccess { players ->
                    _uiState.value = _uiState.value.copy(
                        isSearching = false,
                        searchResults = players,
                        error = null
                    )
                }
                .onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        isSearching = false,
                        searchResults = emptyList(),
                        error = exception.message ?: "Failed to search players"
                    )
                }
        }
    }
    
    fun selectPlayer(player: Player) {
        _uiState.value = _uiState.value.copy(
            selectedPlayer = player,
            searchResults = emptyList()
        )
        loadPlayerStats()
    }
    
    fun updateStatParameters(statGroup: String, statType: String, season: String? = null) {
        val targetSeason = season ?: _uiState.value.currentSeason
        _uiState.value = _uiState.value.copy(
            currentStatGroup = statGroup,
            currentStatType = statType,
            currentSeason = targetSeason
        )
        loadPlayerStats()
    }
    
    private fun loadPlayerStats() {
        val selectedPlayer = _uiState.value.selectedPlayer ?: return
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoadingStats = true,
                error = null
            )
            
            val season = if (_uiState.value.currentStatType == "season") {
                _uiState.value.currentSeason
            } else null
            
            repository.getPlayerStats(
                playerId = selectedPlayer.id,
                statGroup = _uiState.value.currentStatGroup,
                statType = _uiState.value.currentStatType,
                season = season
            )
                .onSuccess { playerWithStats ->
                    val stats = playerWithStats?.stats?.firstOrNull()?.splits ?: emptyList()
                    _uiState.value = _uiState.value.copy(
                        isLoadingStats = false,
                        playerStats = stats,
                        error = null
                    )
                }
                .onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoadingStats = false,
                        playerStats = emptyList(),
                        error = exception.message ?: "Failed to load player stats"
                    )
                }
        }
    }
    
    fun clearSelectedPlayer() {
        _uiState.value = _uiState.value.copy(
            selectedPlayer = null,
            playerStats = emptyList(),
            searchQuery = ""
        )
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}