package com.example.myapplication.data.api

import com.example.myapplication.data.model.PlayerSearchResponse
import com.example.myapplication.data.model.ScheduleResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface MlbApiService {
    
    @GET("schedule")
    suspend fun getSchedule(
        @Query("sportId") sportId: Int = 1,
        @Query("date") date: String,
        @Query("hydrate") hydrate: String = "team,linescore"
    ): Response<ScheduleResponse>
    
    @GET("people/search")
    suspend fun searchPlayers(
        @Query("names") playerName: String,
        @Query("active") active: Boolean = true
    ): Response<PlayerSearchResponse>
    
    @GET("people/{playerId}")
    suspend fun getPlayerStats(
        @Path("playerId") playerId: Int,
        @Query("hydrate") hydrate: String
    ): Response<PlayerSearchResponse>
    
    companion object {
        const val BASE_URL = "https://statsapi.mlb.com/api/v1/"
        
        fun buildStatsHydrate(
            statGroup: String,
            statType: String,
            season: String? = null
        ): String {
            val hydrateParts = mutableListOf(
                "group=[$statGroup]",
                "type=[$statType]"
            )
            
            if (statType == "season" && season != null) {
                hydrateParts.add("season=$season")
            }
            
            return "stats(${hydrateParts.joinToString(",")})"
        }
    }
}