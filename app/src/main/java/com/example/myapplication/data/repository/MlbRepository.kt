package com.example.myapplication.data.repository

import com.example.myapplication.data.api.MlbApiService
import com.example.myapplication.data.model.Game
import com.example.myapplication.data.model.Player
import com.example.myapplication.data.network.NetworkModule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class MlbRepository {
    
    private val apiService = NetworkModule.mlbApiService
    
    suspend fun getGamesByDate(date: String): Result<List<Game>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getSchedule(date = date)
            if (response.isSuccessful) {
                val games = response.body()?.dates?.firstOrNull()?.games ?: emptyList()
                Result.success(games)
            } else {
                Result.failure(Exception("API Error: ${response.code()} - ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun searchPlayers(playerName: String): Result<List<Player>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.searchPlayers(playerName = playerName)
            if (response.isSuccessful) {
                val players = response.body()?.people ?: emptyList()
                Result.success(players)
            } else {
                Result.failure(Exception("API Error: ${response.code()} - ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getPlayerStats(
        playerId: Int,
        statGroup: String,
        statType: String,
        season: String? = null
    ): Result<Player?> = withContext(Dispatchers.IO) {
        try {
            val hydrate = MlbApiService.buildStatsHydrate(statGroup, statType, season)
            val response = apiService.getPlayerStats(playerId, hydrate)
            if (response.isSuccessful) {
                val player = response.body()?.people?.firstOrNull()
                Result.success(player)
            } else {
                Result.failure(Exception("API Error: ${response.code()} - ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    fun getCurrentDate(): String {
        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        return formatter.format(Date())
    }
    
    fun formatGameTime(gameTimeUtc: String?): String {
        if (gameTimeUtc.isNullOrEmpty()) return "Time Unknown"
        
        return try {
            val utcFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
            utcFormat.timeZone = TimeZone.getTimeZone("UTC")
            val date = utcFormat.parse(gameTimeUtc)
            
            val localFormat = SimpleDateFormat("HH:mm", Locale.US)
            localFormat.timeZone = TimeZone.getDefault()
            localFormat.format(date ?: Date())
        } catch (e: Exception) {
            "Time Error"
        }
    }
    
    fun formatGameStatus(game: Game): String {
        return when (game.status.detailedState) {
            "Final", "Game Over" -> {
                val inning = game.linescore?.currentInning
                if (inning != null && inning != 9) "Final ($inning)" else "Final"
            }
            "In Progress" -> {
                val inningOrdinal = game.linescore?.currentInningOrdinal ?: "?"
                val inningState = game.linescore?.inningState ?: ""
                "$inningState $inningOrdinal"
            }
            "Scheduled", "Pre-Game", "Warmup" -> "Scheduled ${formatGameTime(game.gameDate)}"
            else -> {
                when {
                    game.status.detailedState.contains("Postponed") -> "Postponed"
                    game.status.detailedState.contains("Suspended") -> "Suspended"
                    game.status.detailedState.contains("Cancel") -> "Cancelled"
                    else -> game.status.detailedState
                }
            }
        }
    }
}