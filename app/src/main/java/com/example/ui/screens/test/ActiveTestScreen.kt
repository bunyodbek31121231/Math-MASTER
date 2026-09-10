package com.example.ui.screens.test

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.security.SecurityManager
import com.example.ui.components.DifficultyBadge
import com.example.ui.components.OptionChoiceButton
import com.example.ui.viewmodel.TestViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActiveTestScreen(
    viewModel: TestViewModel,
    onTestFinished: (testId: String) -> Unit,
    onExitWithoutSaving: () -> Unit
) {
    // Apply FLAG_SECURE screenshot and recording protection on test screen
    SecurityManager.RequireScreenProtection(enabled = true)

    val state by viewModel.uiState.collectAsState()

    // Intercept back navigation during active test
    BackHandler(enabled = !state.isTestCompleted) {
        viewModel.requestExit()
    }

    // Auto-navigate to result screen when finished
    LaunchedEffect(state.isTestCompleted, state.completedSummary) {
        if (state.isTestCompleted && state.completedSummary != null) {
            onTestFinished(state.completedSummary!!.testId)
        }
    }

    // Exit / Stop Confirmation Dialog
    if (state.showExitDialog) {
        AlertDialog(
            onDismissRequest = viewModel::dismissExitDialog,
            title = { Text(stringResource(R.string.stop_test_dialog_title)) },
            text = { Text(stringResource(R.string.stop_test_dialog_message)) },
            confirmButton = {
                Button(
                    onClick = viewModel::finishTest,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier.testTag("confirm_finish_test_button")
                ) {
                    Text(stringResource(R.string.finish_test_button))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        viewModel.dismissExitDialog()
                        onExitWithoutSaving()
                    },
                    modifier = Modifier.testTag("abandon_test_button")
                ) {
                    Text("Leave Without Saving", color = MaterialTheme.colorScheme.error)
                }
            }
        )
    }

    val currentQuestion = state.questions.getOrNull(state.currentIndex)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Question ${state.currentIndex + 1} of ${state.questions.size}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = viewModel::requestExit,
                        modifier = Modifier.testTag("test_close_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Stop Test"
                        )
                    }
                },
                actions = {
                    // Test Timer
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(end = 16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "%02d:%02d".format(state.secondsElapsed / 60, state.secondsElapsed % 60),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = viewModel::prevQuestion,
                        enabled = state.currentIndex > 0,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("prev_question_button")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Prev")
                    }

                    if (state.currentIndex == state.questions.size - 1) {
                        Button(
                            onClick = viewModel::finishTest,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("submit_test_button")
                        ) {
                            Text(stringResource(R.string.finish_test_button), fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Button(
                            onClick = viewModel::nextQuestion,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("next_question_button")
                        ) {
                            Text("Next")
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            if (state.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (currentQuestion != null) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Question Numbers Horizontal Ribbon
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(vertical = 4.dp)
                    ) {
                        itemsIndexed(state.questions) { index, q ->
                            val isAnswered = state.selectedAnswers.containsKey(q.id)
                            val isCurrent = index == state.currentIndex
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(
                                        when {
                                            isCurrent -> MaterialTheme.colorScheme.primary
                                            isAnswered -> MaterialTheme.colorScheme.tertiary
                                            else -> MaterialTheme.colorScheme.surfaceVariant
                                        }
                                    )
                                    .clickable { viewModel.goToQuestion(index) },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${index + 1}",
                                    color = if (isCurrent || isAnswered) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }

                    // Question Card
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                DifficultyBadge(difficulty = currentQuestion.difficulty)
                                Text(
                                    text = currentQuestion.category,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            Text(
                                text = currentQuestion.question,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontSize = 18.sp,
                                    lineHeight = 26.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            )
                        }
                    }

                    // Choices A, B, C, D (In test mode, answers are NOT revealed immediately)
                    val selected = state.selectedAnswers[currentQuestion.id]
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        OptionChoiceButton(
                            optionLabel = "A",
                            optionText = currentQuestion.optionA,
                            isSelected = selected == "A",
                            isAnswerRevealed = false,
                            isCorrectOption = false,
                            onClick = { viewModel.selectOption("A") }
                        )
                        OptionChoiceButton(
                            optionLabel = "B",
                            optionText = currentQuestion.optionB,
                            isSelected = selected == "B",
                            isAnswerRevealed = false,
                            isCorrectOption = false,
                            onClick = { viewModel.selectOption("B") }
                        )
                        OptionChoiceButton(
                            optionLabel = "C",
                            optionText = currentQuestion.optionC,
                            isSelected = selected == "C",
                            isAnswerRevealed = false,
                            isCorrectOption = false,
                            onClick = { viewModel.selectOption("C") }
                        )
                        OptionChoiceButton(
                            optionLabel = "D",
                            optionText = currentQuestion.optionD,
                            isSelected = selected == "D",
                            isAnswerRevealed = false,
                            isCorrectOption = false,
                            onClick = { viewModel.selectOption("D") }
                        )
                    }

                    Spacer(modifier = Modifier.height(30.dp))
                }
            }
        }
    }
}
