package com.example.myapplication.data.model

import com.google.gson.annotations.SerializedName

data class ScheduleResponse(
    val dates: List<GameDate>
)

data class GameDate(
    val date: String,
    val games: List<Game>
)

data class Game(
    val gamePk: Int,
    val gameDate: String,
    val status: GameStatus,
    val teams: Teams,
    val linescore: LineScore?,
    val content: GameContent?
)

data class GameStatus(
    val detailedState: String,
    val abstractGameState: String
)

data class Teams(
    val away: TeamInfo,
    val home: TeamInfo
)

data class TeamInfo(
    val team: Team,
    val score: Int?
)

data class Team(
    val id: Int,
    val name: String,
    val abbreviation: String?
)

data class LineScore(
    val currentInning: Int?,
    val currentInningOrdinal: String?,
    val inningState: String?,
    val teams: LineScoreTeams?
)

data class LineScoreTeams(
    val home: LineScoreTeam?,
    val away: LineScoreTeam?
)

data class LineScoreTeam(
    val runs: Int?,
    val hits: Int?,
    val errors: Int?
)

data class GameContent(
    val summary: Any? = null  // 可以是字符串或对象，忽略解析
)