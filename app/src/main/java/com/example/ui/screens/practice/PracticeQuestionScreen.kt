package com.example.ui.screens.practice

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
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
import com.example.ui.components.MathFormulaBox
import com.example.ui.components.OptionChoiceButton
import com.example.ui.theme.CorrectGreen
import com.example.ui.theme.IncorrectRed
import com.example.ui.viewmodel.PracticeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PracticeQuestionScreen(
    viewModel: PracticeViewModel,
    onNavigateBack: () -> Unit
) {
    // Apply FLAG_SECURE screenshot and recording protection on question screen
    SecurityManager.RequireScreenProtection(enabled = true)

    val state by viewModel.uiState.collectAsState()
    val problem = state.currentProblem

    if (state.isSessionFinished) {
        AlertDialog(
            onDismissRequest = onNavigateBack,
            title = { Text(stringResource(R.string.practice_session_summary)) },
            text = {
                Column {
                    Text("Problems Solved: ${state.solvedInSession}")
                    Text("Correct Answers: ${state.correctInSession}")
                    val acc = if (state.solvedInSession > 0)
                        (state.correctInSession.toDouble() / state.solvedInSession.toDouble()) * 100.0
                    else 0.0
                    Text("Session Accuracy: %.0f%%".format(acc))
                }
            },
            confirmButton = {
                Button(
                    onClick = onNavigateBack,
                    modifier = Modifier.testTag("session_summary_ok_button")
                ) {
                    Text("Done")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = problem?.category ?: "Mathematics Practice",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "No-Repeat Engine Active",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("practice_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    // Timer display
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(end = 12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "%02d:%02d".format(state.secondsElapsed / 60, state.secondsElapsed % 60),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
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
                    CircularProgressIndicator(modifier = Modifier.testTag("loading_indicator"))
                }
            } else if (problem != null) {
                val difficulty = com.example.model.Difficulty.fromString(problem.difficulty)
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Question Header Card
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
                                DifficultyBadge(difficulty = problem.difficulty)

                                Text(
                                    text = "Score: ${state.correctInSession}/${state.solvedInSession}",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            Text(
                                text = problem.question,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontSize = 18.sp,
                                    lineHeight = 26.sp,
                                    fontWeight = FontWeight.Medium
                                ),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    // Options A, B, C, D
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        OptionChoiceButton(
                            optionLabel = "A",
                            optionText = problem.optionA,
                            isSelected = state.selectedOption == "A",
                            isAnswerRevealed = state.isAnswerRevealed,
                            isCorrectOption = problem.correctAnswer == "A",
                            onClick = { viewModel.selectOption("A") }
                        )
                        OptionChoiceButton(
                            optionLabel = "B",
                            optionText = problem.optionB,
                            isSelected = state.selectedOption == "B",
                            isAnswerRevealed = state.isAnswerRevealed,
                            isCorrectOption = problem.correctAnswer == "B",
                            onClick = { viewModel.selectOption("B") }
                        )
                        OptionChoiceButton(
                            optionLabel = "C",
                            optionText = problem.optionC,
                            isSelected = state.selectedOption == "C",
                            isAnswerRevealed = state.isAnswerRevealed,
                            isCorrectOption = problem.correctAnswer == "C",
                            onClick = { viewModel.selectOption("C") }
                        )
                        OptionChoiceButton(
                            optionLabel = "D",
                            optionText = problem.optionD,
                            isSelected = state.selectedOption == "D",
                            isAnswerRevealed = state.isAnswerRevealed,
                            isCorrectOption = problem.correctAnswer == "D",
                            onClick = { viewModel.selectOption("D") }
                        )
                    }

                    // Action Button (Submit Answer OR Next Problem)
                    if (!state.isAnswerRevealed) {
                        Button(
                            onClick = viewModel::submitAnswer,
                            enabled = state.selectedOption != null,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("submit_answer_button")
                        ) {
                            Text(
                                text = stringResource(R.string.submit_answer),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Solution & Explanation Revealed Section
                    AnimatedVisibility(visible = state.isAnswerRevealed) {
                        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                            // Feedback Banner
                            Surface(
                                color = if (state.isAnswerCorrect) CorrectGreen.copy(alpha = 0.15f)
                                else IncorrectRed.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = if (state.isAnswerCorrect) Icons.Default.Check else Icons.Default.Close,
                                        contentDescription = null,
                                        tint = if (state.isAnswerCorrect) CorrectGreen else IncorrectRed
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = if (state.isAnswerCorrect) "Correct! +${difficulty.baseXP + 15} XP Earned"
                                        else "Incorrect. Correct option was (${problem.correctAnswer})",
                                        fontWeight = FontWeight.Bold,
                                        color = if (state.isAnswerCorrect) CorrectGreen else IncorrectRed
                                    )
                                }
                            }

                            // Formula Box if available
                            if (!problem.formula.isNullOrBlank()) {
                                MathFormulaBox(formula = problem.formula)
                            }

                            // Step-by-Step Solution Card
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surface
                                ),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        text = stringResource(R.string.solution_title),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = problem.solution,
                                        style = MaterialTheme.typography.bodyMedium,
                                        lineHeight = 22.sp
                                    )

                                    if (problem.explanation != problem.solution && problem.explanation.isNotBlank()) {
                                        Spacer(modifier = Modifier.height(12.dp))
                                        Text(
                                            text = stringResource(R.string.explanation_title),
                                            style = MaterialTheme.typography.labelLarge,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.secondary
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = problem.explanation,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }

                            // Next Problem & Finish Session Buttons
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                OutlinedButton(
                                    onClick = viewModel::finishSession,
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(50.dp)
                                        .testTag("finish_session_button")
                                ) {
                                    Text("Finish")
                                }

                                Button(
                                    onClick = viewModel::loadNextProblem,
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .weight(1.5f)
                                        .height(50.dp)
                                        .testTag("next_problem_button")
                                ) {
                                    Text(
                                        text = stringResource(R.string.next_problem),
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = state.errorMessage ?: "No problems found.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
