package com.example.ui.screens.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.ui.admin.AdminViewModel
import com.example.util.admin.AnalyzedMaterial

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminEditorScreen(
    index: Int,
    viewModel: AdminViewModel,
    onNavigateBack: () -> Unit
) {
    val materials by viewModel.previewMaterials.collectAsState()
    val material = materials.getOrNull(index) ?: return

    var title by remember { mutableStateOf(material.title ?: "") }
    var body by remember { mutableStateOf(material.body ?: "") }
    var question by remember { mutableStateOf(material.question ?: "") }
    var optionA by remember { mutableStateOf(material.optionA ?: "") }
    var optionB by remember { mutableStateOf(material.optionB ?: "") }
    var optionC by remember { mutableStateOf(material.optionC ?: "") }
    var optionD by remember { mutableStateOf(material.optionD ?: "") }
    var correctAnswer by remember { mutableStateOf(material.correctAnswer ?: "A") }
    var solution by remember { mutableStateOf(material.solution ?: "") }
    var explanation by remember { mutableStateOf(material.explanation ?: "") }
    var formula by remember { mutableStateOf(material.formula ?: "") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Material Tahrirlash") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        val updated = material.copy(
                            title = title,
                            body = body,
                            question = question,
                            optionA = optionA,
                            optionB = optionB,
                            optionC = optionC,
                            optionD = optionD,
                            correctAnswer = correctAnswer,
                            solution = solution,
                            explanation = explanation,
                            formula = formula,
                            isUncertain = false
                        )
                        viewModel.updateMaterial(index, updated)
                        onNavigateBack()
                    }) {
                        Icon(Icons.Default.Save, contentDescription = "Save")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(text = "Turi: ${material.type}", style = MaterialTheme.typography.labelLarge)

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Sarlavha") },
                modifier = Modifier.fillMaxWidth()
            )

            if (material.type == "TEST") {
                OutlinedTextField(
                    value = question,
                    onValueChange = { question = it },
                    label = { Text("Savol") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )
                
                OutlinedTextField(
                    value = optionA,
                    onValueChange = { optionA = it },
                    label = { Text("A varianti") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = optionB,
                    onValueChange = { optionB = it },
                    label = { Text("B varianti") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = optionC,
                    onValueChange = { optionC = it },
                    label = { Text("C varianti") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = optionD,
                    onValueChange = { optionD = it },
                    label = { Text("D varianti") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                OutlinedTextField(
                    value = correctAnswer,
                    onValueChange = { correctAnswer = it },
                    label = { Text("To'g'ri javob (A/B/C/D)") },
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                OutlinedTextField(
                    value = body,
                    onValueChange = { body = it },
                    label = { Text("Matn") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 5
                )
            }

            OutlinedTextField(
                value = formula,
                onValueChange = { formula = it },
                label = { Text("Formula") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = solution,
                onValueChange = { solution = it },
                label = { Text("Yechim") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )

            OutlinedTextField(
                value = explanation,
                onValueChange = { explanation = it },
                label = { Text("Izoh") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2
            )
            
            Button(
                onClick = {
                    val updated = material.copy(
                        title = title,
                        body = body,
                        question = question,
                        optionA = optionA,
                        optionB = optionB,
                        optionC = optionC,
                        optionD = optionD,
                        correctAnswer = correctAnswer,
                        solution = solution,
                        explanation = explanation,
                        formula = formula,
                        isUncertain = false
                    )
                    viewModel.updateMaterial(index, updated)
                    onNavigateBack()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("SAQLASH")
            }
        }
    }
}
