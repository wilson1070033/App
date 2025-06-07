package com.example.myapplication.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapplication.data.model.Game
import com.example.myapplication.data.repository.MlbRepository
import com.example.myapplication.ui.viewmodel.GamesViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GamesScreen(
    onNavigateBack: () -> Unit,
    viewModel: GamesViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val repository = remember { MlbRepository() }
    
    // Date picker state
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()
    
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        TopAppBar(
            title = { Text("MLB Games") },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back"
                    )
                }
            },
            actions = {
                IconButton(onClick = { viewModel.loadTodaysGames() }) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh"
                    )
                }
            }
        )
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Beautiful date selection card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Select Game Date",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Date picker button
                        OutlinedCard(
                            onClick = { showDatePicker = true },
                            modifier = Modifier.weight(1f),
                            colors = CardDefaults.outlinedCardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DateRange,
                                    contentDescription = "Select Date",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (uiState.selectedDate.isNotEmpty()) 
                                        formatDisplayDate(uiState.selectedDate) 
                                    else "Pick Date",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                        
                        // Today button
                        FilledTonalButton(
                            onClick = { viewModel.loadTodaysGames() }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Home,
                                contentDescription = "Today",
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Today")
                        }
                        
                        // Refresh button
                        IconButton(
                            onClick = { 
                                if (uiState.selectedDate.isNotEmpty()) {
                                    viewModel.loadGamesByDate(uiState.selectedDate)
                                } else {
                                    viewModel.loadTodaysGames()
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Refresh",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Content
            when {
                uiState.isLoading -> {
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
                
                uiState.games.isEmpty() -> {
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No games found for ${uiState.selectedDate}",
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }
                
                else -> {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(uiState.games) { game ->
                            GameCard(
                                game = game,
                                repository = repository
                            )
                        }
                    }
                }
            }
        }
        
        // Date picker dialog
        if (showDatePicker) {
            DatePickerDialog(
                onDateSelected = { selectedDate ->
                    viewModel.loadGamesByDate(selectedDate)
                    showDatePicker = false
                },
                onDismiss = { showDatePicker = false },
                datePickerState = datePickerState
            )
        }
    }
}

@Composable
fun GameCard(
    game: Game,
    repository: MlbRepository
) {
    val isGameCompleted = game.status.detailedState in listOf("Final", "Game Over") || 
                         game.status.detailedState.contains("Completed")
    val isGameInProgress = game.status.detailedState == "In Progress"
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        colors = CardDefaults.cardColors(
            containerColor = when {
                isGameInProgress -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.1f)
                isGameCompleted -> MaterialTheme.colorScheme.surface
                else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            }
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            // Game status with background
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = when {
                    isGameInProgress -> MaterialTheme.colorScheme.error
                    isGameCompleted -> MaterialTheme.colorScheme.primary
                    else -> MaterialTheme.colorScheme.secondary
                },
                modifier = Modifier.clip(RoundedCornerShape(8.dp))
            ) {
                Text(
                    text = repository.formatGameStatus(game),
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Teams and scores in a more structured layout
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    // Away team
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                modifier = Modifier.size(8.dp)
                            ) {}
                            
                            Spacer(modifier = Modifier.width(8.dp))
                            
                            Column {
                                Text(
                                    text = game.teams.away.team.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Away",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        
                        if (game.teams.away.score != null) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.primaryContainer
                            ) {
                                Text(
                                    text = "${game.teams.away.score}",
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Divider
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                        thickness = 1.dp
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Home team
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f),
                                modifier = Modifier.size(8.dp)
                            ) {}
                            
                            Spacer(modifier = Modifier.width(8.dp))
                            
                            Column {
                                Text(
                                    text = game.teams.home.team.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Home",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        
                        if (game.teams.home.score != null) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.secondaryContainer
                            ) {
                                Text(
                                    text = "${game.teams.home.score}",
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                                )
                            }
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Game ID in a subtle way
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
                ) {
                    Text(
                        text = "Game #${game.gamePk}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerDialog(
    onDateSelected: (String) -> Unit,
    onDismiss: () -> Unit,
    datePickerState: DatePickerState
) {
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val calendar = Calendar.getInstance()
                        calendar.timeInMillis = millis
                        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                        onDateSelected(formatter.format(calendar.time))
                    }
                }
            ) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    ) {
        DatePicker(
            state = datePickerState,
            title = {
                Text(
                    text = "Select Game Date",
                    modifier = Modifier.padding(16.dp)
                )
            }
        )
    }
}

private fun formatDisplayDate(dateString: String): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val outputFormat = SimpleDateFormat("MMM dd, yyyy", Locale.US)
        val date = inputFormat.parse(dateString)
        date?.let { outputFormat.format(it) } ?: dateString
    } catch (e: Exception) {
        dateString
    }
}