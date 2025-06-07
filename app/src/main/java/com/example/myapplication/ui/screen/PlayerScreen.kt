package com.example.myapplication.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapplication.data.model.Player
import com.example.myapplication.data.model.PlayerStats
import com.example.myapplication.data.model.StatSplit
import com.example.myapplication.ui.viewmodel.PlayerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerScreen(
    onNavigateBack: () -> Unit,
    viewModel: PlayerViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        TopAppBar(
            title = { Text("Player Statistics") },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back"
                    )
                }
            }
        )
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Search bar
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = { viewModel.searchPlayers(it) },
                label = { Text("Search Players") },
                placeholder = { Text("Enter player name (e.g., Shohei Ohtani)") },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Search, contentDescription = "Search")
                },
                trailingIcon = {
                    if (uiState.searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.searchPlayers("") }) {
                            Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Content
            when {
                uiState.isSearching -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                
                uiState.error != null -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "Error",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Text(
                                text = uiState.error ?: "Unknown error",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }
                
                uiState.selectedPlayer != null -> {
                    uiState.selectedPlayer?.let { selectedPlayer ->
                        PlayerStatsView(
                            player = selectedPlayer,
                            stats = uiState.playerStats,
                            currentStatGroup = uiState.currentStatGroup,
                            currentStatType = uiState.currentStatType,
                            currentSeason = uiState.currentSeason,
                            isLoadingStats = uiState.isLoadingStats,
                            onStatParametersChanged = { group, type, season ->
                                viewModel.updateStatParameters(group, type, season)
                            },
                            onClearPlayer = { viewModel.clearSelectedPlayer() }
                        )
                    }
                }
                
                uiState.searchResults.isNotEmpty() -> {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(uiState.searchResults) { player ->
                            PlayerCard(
                                player = player,
                                onPlayerSelected = { viewModel.selectPlayer(player) }
                            )
                        }
                    }
                }
                
                uiState.searchQuery.isNotEmpty() && uiState.searchResults.isEmpty() -> {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No players found for '${uiState.searchQuery}'",
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }
                
                else -> {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Search for MLB players to view their statistics",
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerCard(
    player: Player,
    onPlayerSelected: () -> Unit
) {
    Card(
        onClick = onPlayerSelected,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = player.fullName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Team: ${player.currentTeam?.name ?: "Free Agent"}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "Pos: ${player.primaryPosition?.abbreviation ?: "N/A"}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            
            Text(
                text = "ID: ${player.id}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun PlayerStatsView(
    player: Player,
    stats: List<StatSplit>,
    currentStatGroup: String,
    currentStatType: String,
    currentSeason: String,
    isLoadingStats: Boolean,
    onStatParametersChanged: (String, String, String?) -> Unit,
    onClearPlayer: () -> Unit
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            // Player info header
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = player.fullName,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "${player.currentTeam?.name ?: "Free Agent"} • ${player.primaryPosition?.abbreviation ?: "N/A"}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    
                    TextButton(onClick = onClearPlayer) {
                        Text("Clear")
                    }
                }
            }
        }
        
        item {
            // Stat parameter controls
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Statistics Options",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Stat group selection
                    Text(
                        text = "Category:",
                        style = MaterialTheme.typography.labelMedium
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            onClick = { onStatParametersChanged("hitting", currentStatType, currentSeason) },
                            label = { Text("Hitting") },
                            selected = currentStatGroup == "hitting"
                        )
                        FilterChip(
                            onClick = { onStatParametersChanged("pitching", currentStatType, currentSeason) },
                            label = { Text("Pitching") },
                            selected = currentStatGroup == "pitching"
                        )
                        FilterChip(
                            onClick = { onStatParametersChanged("fielding", currentStatType, currentSeason) },
                            label = { Text("Fielding") },
                            selected = currentStatGroup == "fielding"
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Stat type selection
                    Text(
                        text = "Time Range:",
                        style = MaterialTheme.typography.labelMedium
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            onClick = { onStatParametersChanged(currentStatGroup, "season", currentSeason) },
                            label = { Text("Season") },
                            selected = currentStatType == "season"
                        )
                        FilterChip(
                            onClick = { onStatParametersChanged(currentStatGroup, "career", null) },
                            label = { Text("Career") },
                            selected = currentStatType == "career"
                        )
                        FilterChip(
                            onClick = { onStatParametersChanged(currentStatGroup, "yearByYear", null) },
                            label = { Text("Year by Year") },
                            selected = currentStatType == "yearByYear"
                        )
                    }
                    
                    if (currentStatType == "season") {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Season: $currentSeason",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
        
        item {
            // Stats display
            if (isLoadingStats) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            } else if (stats.isEmpty()) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No statistics found for the selected parameters",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            } else {
                stats.forEach { statSplit ->
                    StatSplitCard(
                        statSplit = statSplit,
                        statGroup = currentStatGroup
                    )
                }
            }
        }
    }
}

@Composable
fun StatSplitCard(
    statSplit: StatSplit,
    statGroup: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header with team/season info
            if (statSplit.season != null || statSplit.team != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    if (statSplit.season != null) {
                        Text(
                            text = "Season: ${statSplit.season}",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    if (statSplit.team != null) {
                        Text(
                            text = statSplit.team.name,
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }
            
            // Stats based on group
            when (statGroup) {
                "hitting" -> HittingStatsDisplay(statSplit.stat)
                "pitching" -> PitchingStatsDisplay(statSplit.stat)
                "fielding" -> FieldingStatsDisplay(statSplit.stat)
            }
        }
    }
}

@Composable
fun HittingStatsDisplay(stats: PlayerStats) {
    Column {
        StatsRow("Games", stats.gamesPlayed?.toString() ?: "-", "At Bats", stats.atBats?.toString() ?: "-")
        StatsRow("Runs", stats.runs?.toString() ?: "-", "Hits", stats.hits?.toString() ?: "-")
        StatsRow("2B", stats.doubles?.toString() ?: "-", "3B", stats.triples?.toString() ?: "-")
        StatsRow("HR", stats.homeRuns?.toString() ?: "-", "RBI", stats.rbi?.toString() ?: "-")
        StatsRow("BB", stats.baseOnBalls?.toString() ?: "-", "SO", stats.strikeOuts?.toString() ?: "-")
        StatsRow("SB", stats.stolenBases?.toString() ?: "-", "CS", stats.caughtStealing?.toString() ?: "-")
        StatsRow("AVG", stats.avg ?: "-", "OBP", stats.obp ?: "-")
        StatsRow("SLG", stats.slg ?: "-", "OPS", stats.ops ?: "-")
    }
}

@Composable
fun PitchingStatsDisplay(stats: PlayerStats) {
    Column {
        StatsRow("Games", stats.gamesPitched?.toString() ?: "-", "Starts", stats.gamesStarted?.toString() ?: "-")
        StatsRow("Wins", stats.wins?.toString() ?: "-", "Losses", stats.losses?.toString() ?: "-")
        StatsRow("ERA", stats.era ?: "-", "Saves", stats.saves?.toString() ?: "-")
        StatsRow("IP", stats.inningsPitched ?: "-", "Hits", stats.hits?.toString() ?: "-")
        StatsRow("Runs", stats.runs?.toString() ?: "-", "ER", stats.earnedRuns?.toString() ?: "-")
        StatsRow("BB", stats.baseOnBalls?.toString() ?: "-", "SO", stats.strikeOuts?.toString() ?: "-")
        StatsRow("WHIP", stats.whip ?: "-", "", "")
    }
}

@Composable
fun FieldingStatsDisplay(stats: PlayerStats) {
    Column {
        StatsRow("Games", stats.gamesPlayed?.toString() ?: "-", "Starts", stats.gamesStarted?.toString() ?: "-")
        StatsRow("Innings", stats.innings ?: "-", "Chances", stats.chances?.toString() ?: "-")
        StatsRow("Assists", stats.assists?.toString() ?: "-", "Putouts", stats.putOuts?.toString() ?: "-")
        StatsRow("Errors", stats.errors?.toString() ?: "-", "Double Plays", stats.doublePlays?.toString() ?: "-")
        StatsRow("Fielding %", stats.fielding ?: "-", "", "")
    }
}

@Composable
fun StatsRow(
    label1: String,
    value1: String,
    label2: String,
    value2: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        if (label1.isNotEmpty()) {
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = label1,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = value1,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        } else {
            Spacer(modifier = Modifier.weight(1f))
        }
        
        if (label2.isNotEmpty()) {
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = label2,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = value2,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        } else {
            Spacer(modifier = Modifier.weight(1f))
        }
    }
    
    Spacer(modifier = Modifier.height(8.dp))
}