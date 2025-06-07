package com.example.myapplication.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.model.Game
import com.example.myapplication.data.repository.MlbRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class GamesUiState(
    val isLoading: Boolean = false,
    val games: List<Game> = emptyList(),
    val error: String? = null,
    val selectedDate: String = ""
)

class GamesViewModel : ViewModel() {
    
    private val repository = MlbRepository()
    
    private val _uiState = MutableStateFlow(GamesUiState())
    val uiState: StateFlow<GamesUiState> = _uiState.asStateFlow()
    
    init {
        loadTodaysGames()
    }
    
    fun loadTodaysGames() {
        val today = repository.getCurrentDate()
        loadGamesByDate(today)
    }
    
    fun loadGamesByDate(date: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                error = null,
                selectedDate = date
            )
            
            repository.getGamesByDate(date)
                .onSuccess { games ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        games = games,
                        error = null
                    )
                }
                .onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        games = emptyList(),
                        error = exception.message ?: "Unknown error occurred"
                    )
                }
        }
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}