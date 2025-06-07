package com.example.myapplication.data.model

import com.google.gson.annotations.SerializedName

data class PlayerSearchResponse(
    val people: List<Player>
)

data class Player(
    val id: Int,
    val fullName: String,
    val currentTeam: Team?,
    val primaryPosition: Position?,
    val stats: List<StatGroup>? = null
)

data class Position(
    val code: String,
    val name: String,
    val type: String,
    val abbreviation: String
)

data class StatGroup(
    val type: StatType,
    val group: StatGroupType,
    val splits: List<StatSplit>
)

data class StatType(
    val displayName: String
)

data class StatGroupType(
    val displayName: String
)

data class StatSplit(
    val season: String?,
    val team: Team?,
    val position: Position?,
    val stat: PlayerStats
)

data class PlayerStats(
    // Common stats
    val gamesPlayed: Int? = null,
    
    // Hitting stats
    val atBats: Int? = null,
    val runs: Int? = null,
    val hits: Int? = null,
    val doubles: Int? = null,
    val triples: Int? = null,
    val homeRuns: Int? = null,
    val rbi: Int? = null,
    val baseOnBalls: Int? = null,
    val strikeOuts: Int? = null,
    val stolenBases: Int? = null,
    val caughtStealing: Int? = null,
    val avg: String? = null,
    val obp: String? = null,
    val slg: String? = null,
    val ops: String? = null,
    
    // Pitching stats
    val gamesPitched: Int? = null,
    val gamesStarted: Int? = null,
    val wins: Int? = null,
    val losses: Int? = null,
    val era: String? = null,
    val saves: Int? = null,
    val inningsPitched: String? = null,
    val earnedRuns: Int? = null,
    val whip: String? = null,
    
    // Fielding stats
    val innings: String? = null,
    val chances: Int? = null,
    val assists: Int? = null,
    val putOuts: Int? = null,
    val errors: Int? = null,
    val doublePlays: Int? = null,
    val fielding: String? = null
)