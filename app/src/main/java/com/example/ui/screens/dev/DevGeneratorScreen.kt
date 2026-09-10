package com.example.ui.screens.dev

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Difficulty
import com.example.ui.components.MathTopAppBar

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DevGeneratorScreen(
    viewModel: DevGeneratorViewModel,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    val allCategories = listOf(
        "Algebra",
        "Trigonometry",
        "Geometry",
        "Number Theory",
        "Probability",
        "Combinatorics",
        "Statistics",
        "Calculus",
        "Logic",
        "Olympiad",
        "Mixed"
    )

    val selectedCategories = remember { mutableStateListOf<String>().apply { addAll(allCategories) } }
    val selectedDifficulties = remember { mutableStateListOf<Difficulty>().apply { addAll(Difficulty.entries) } }
    var selectedBatchSize by remember { mutableIntStateOf(500) }
    var selectedTargetTotal by remember { mutableIntStateOf(20620) }

    Scaffold(
        topBar = {
            MathTopAppBar(
                title = "Developer Problem Generator",
                onBackClick = onNavigateBack
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Info
            item {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("dev_header_card")
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Admin Engine: Production Pipeline (20,620+ Target)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Controlled resumable batch pipeline with QuotaManager distribution, SHA-256 deduplication, and transactional Room commits.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                        )
                    }
                }
            }

            // Real-Time Database Statistics Card
            item {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("db_stats_card")
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Database Content Statistics",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Button(
                                onClick = { viewModel.refreshDatabaseStats() },
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                modifier = Modifier.testTag("refresh_db_stats_button")
                            ) {
                                Text("Refresh", fontSize = 12.sp)
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Total Verified Problems: ${state.totalInDb} / 20,620 target",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        val progressToMilestone = (state.totalInDb.toFloat() / 20620f).coerceIn(0f, 1f)
                        LinearProgressIndicator(
                            progress = { progressToMilestone },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .testTag("milestone_progress_bar")
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        if (state.categoryBreakdown.isNotEmpty()) {
                            Text(
                                text = "Category Distribution:",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                state.categoryBreakdown.forEach { (cat, count) ->
                                    Text(
                                        text = "$cat: $count",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.9f)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Target Milestone Selection
            item {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().testTag("target_milestone_card")
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Target Goal (Problems)",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(500, 1000, 5000, 10000, 20620).forEach { count ->
                                FilterChip(
                                    selected = selectedTargetTotal == count,
                                    onClick = { selectedTargetTotal = count },
                                    label = { Text(if (count == 20620) "20,620 (Full)" else "$count") },
                                    modifier = Modifier.testTag("target_goal_$count")
                                )
                            }
                        }
                    }
                }
            }

            // Batch Size Selection (Controlled safety batches: 100, 250, 500, 1000)
            item {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().testTag("batch_size_card")
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Controlled Batch Size (Safe Commits)",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Inserts are committed in isolated Room transactions. Default is 500.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(100, 250, 500, 1000).forEach { count ->
                                FilterChip(
                                    selected = selectedBatchSize == count,
                                    onClick = { selectedBatchSize = count },
                                    label = { Text("$count") },
                                    modifier = Modifier.testTag("batch_size_$count")
                                )
                            }
                        }
                    }
                }
            }

            // Category Selection
            item {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Target Categories (${selectedCategories.size}/${allCategories.size})",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            allCategories.forEach { cat ->
                                val isSelected = selectedCategories.contains(cat)
                                FilterChip(
                                    selected = isSelected,
                                    onClick = {
                                        if (isSelected) {
                                            if (selectedCategories.size > 1) selectedCategories.remove(cat)
                                        } else {
                                            selectedCategories.add(cat)
                                        }
                                    },
                                    label = { Text(cat) },
                                    modifier = Modifier.testTag("filter_cat_$cat")
                                )
                            }
                        }
                    }
                }
            }

            // Difficulties Selection
            item {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Difficulties",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Difficulty.entries.forEach { diff ->
                                val isSelected = selectedDifficulties.contains(diff)
                                FilterChip(
                                    selected = isSelected,
                                    onClick = {
                                        if (isSelected) {
                                            if (selectedDifficulties.size > 1) selectedDifficulties.remove(diff)
                                        } else {
                                            selectedDifficulties.add(diff)
                                        }
                                    },
                                    label = { Text(diff.name) },
                                    modifier = Modifier.testTag("filter_diff_${diff.name}")
                                )
                            }
                        }
                    }
                }
            }

            // Action Buttons & Progress Controls
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    if (state.isGenerating) {
                        Text(
                            text = "Generating ${state.progressCurrent} / ${state.progressTotal} (Batch chunk: ${state.currentBatchProgress})...",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        val progress = if (state.progressTotal > 0) state.progressCurrent.toFloat() / state.progressTotal else 0f
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .testTag("generation_progress_bar")
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = { viewModel.pauseGeneration() },
                                modifier = Modifier.weight(1f).testTag("pause_generation_button")
                            ) {
                                Text("Pause Job")
                            }
                            OutlinedButton(
                                onClick = { viewModel.cancelGeneration() },
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                                modifier = Modifier.weight(1f).testTag("cancel_generation_button")
                            ) {
                                Text("Cancel Job")
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    } else {
                        Button(
                            onClick = {
                                viewModel.startOrResumeJob(
                                    targetTotal = selectedTargetTotal,
                                    batchSize = selectedBatchSize,
                                    categories = selectedCategories.toList(),
                                    difficulties = selectedDifficulties.toList()
                                )
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("start_generation_button")
                        ) {
                            Text(
                                text = if (state.activeJob != null && state.activeJob!!.status == "PAUSED") {
                                    "Resume Pipeline (${state.activeJob!!.completedCount} / ${state.activeJob!!.targetCount})"
                                } else {
                                    "Start Controlled Pipeline ($selectedBatchSize batch size)"
                                },
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = state.statusMessage,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Database Integrity Verification Utility
            item {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("integrity_check_card")
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Database Integrity & Quality Assurance",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Checks mathematical correctness, four distinct choices, valid answer letters, and SHA-256 uniqueness across records.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        Button(
                            onClick = { viewModel.runIntegrityCheck() },
                            enabled = !state.isCheckingIntegrity,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth().testTag("run_integrity_button")
                        ) {
                            Text(if (state.isCheckingIntegrity) "Running Integrity Check..." else "Run Database Integrity Check")
                        }

                        state.integrityReport?.let { report ->
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = if (report.isValid) "Status: PASSED (100% Valid)" else "Status: WARNINGS DETECTED",
                                fontWeight = FontWeight.Bold,
                                color = if (report.isValid) Color(0xFF2E7D32) else MaterialTheme.colorScheme.error
                            )
                            Text("Total Checked: ${report.totalChecked}", fontSize = 12.sp)
                            Text("Healthy Problems: ${report.healthyCount}", fontSize = 12.sp)
                            Text("Malformed: ${report.malformedCount}", fontSize = 12.sp)
                            Text("Duplicate Hashes: ${report.duplicateHashCount}", fontSize = 12.sp)

                            if (report.issues.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Issues Found:", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                                report.issues.take(5).forEach { issue ->
                                    Text("• $issue", fontSize = 11.sp, color = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    }
                }
            }

            // Last Run Statistics
            state.lastStats?.let { stats ->
                item {
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("stats_summary_card")
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Pipeline Batch Results",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Requested: ${stats.requested}")
                                Text("Generated: ${stats.generated}")
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Valid: ${stats.valid}", color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold)
                                Text("Inserted: ${stats.inserted}", color = Color(0xFF1565C0), fontWeight = FontWeight.Bold)
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Duplicates Skipped: ${stats.duplicates}")
                                Text("Invalid Rejected: ${stats.invalid}")
                            }

                            if (stats.byCategory.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("By Category:", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                                stats.byCategory.forEach { (cat, cnt) ->
                                    Text("• $cat: $cnt", fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
