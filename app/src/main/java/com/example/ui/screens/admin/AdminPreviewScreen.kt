package com.example.ui.screens.admin

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.admin.AdminUiState
import com.example.ui.admin.AdminViewModel
import com.example.util.admin.AnalyzedMaterial

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminPreviewScreen(
    viewModel: AdminViewModel,
    onNavigateToEditor: (Int) -> Unit,
    onNavigateBack: () -> Unit,
    onImportFinished: () -> Unit
) {
    val materials by viewModel.previewMaterials.collectAsState()
    val selectedIndices by viewModel.selectedIndices.collectAsState()
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Preview (${materials.size})") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        bottomBar = {
            Surface(tonalElevation = 4.dp) {
                Button(
                    onClick = { viewModel.finalizeImport("Imported_File") },
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    enabled = selectedIndices.isNotEmpty() && uiState !is AdminUiState.Loading
                ) {
                    if (uiState is AdminUiState.Loading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("TANLANGANLARNI MAXSUSGA IMPORT QILISH")
                    }
                }
            }
        }
    ) { padding ->
        if (uiState is AdminUiState.Success && (uiState as AdminUiState.Success).message.contains("import qilindi")) {
            onImportFinished()
        }

        Column(
            modifier = Modifier.fillMaxSize().padding(padding)
        ) {
            // Category Quick Selection Chips
            val topicCount = materials.count { it.type == "TOPIC" }
            val exampleCount = materials.count { it.type == "EXAMPLE" }
            val exerciseCount = materials.count { it.type == "EXERCISE" }
            val testCount = materials.count { it.type == "TEST" }
            val theoryCount = materials.count { it.type == "THEORY" || it.type == "FORMULA" }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (topicCount > 0) {
                    FilterChip(
                        selected = materials.indices.filter { materials[it].type == "TOPIC" }.all { selectedIndices.contains(it) },
                        onClick = { viewModel.toggleSelectionByType("TOPIC") },
                        label = { Text("Mavzu ($topicCount)") }
                    )
                }
                if (exampleCount > 0) {
                    FilterChip(
                        selected = materials.indices.filter { materials[it].type == "EXAMPLE" }.all { selectedIndices.contains(it) },
                        onClick = { viewModel.toggleSelectionByType("EXAMPLE") },
                        label = { Text("Misol ($exampleCount)") }
                    )
                }
                if (exerciseCount > 0) {
                    FilterChip(
                        selected = materials.indices.filter { materials[it].type == "EXERCISE" }.all { selectedIndices.contains(it) },
                        onClick = { viewModel.toggleSelectionByType("EXERCISE") },
                        label = { Text("Mashq ($exerciseCount)") }
                    )
                }
                if (testCount > 0) {
                    FilterChip(
                        selected = materials.indices.filter { materials[it].type == "TEST" }.all { selectedIndices.contains(it) },
                        onClick = { viewModel.toggleSelectionByType("TEST") },
                        label = { Text("Test ($testCount)") }
                    )
                }
                if (theoryCount > 0) {
                    FilterChip(
                        selected = materials.indices.filter { materials[it].type == "THEORY" || materials[it].type == "FORMULA" }.all { selectedIndices.contains(it) },
                        onClick = { viewModel.toggleSelectionByType("THEORY_FORMULA") },
                        label = { Text("Nazariya ($theoryCount)") }
                    )
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                itemsIndexed(materials) { index, material ->
                    val isSelected = selectedIndices.contains(index)
                    PreviewMaterialCard(
                        material = material,
                        isSelected = isSelected,
                        onToggle = { viewModel.toggleSelection(index) },
                        onEdit = { onNavigateToEditor(index) }
                    )
                }
            }
        }
    }
}

@Composable
fun PreviewMaterialCard(
    material: AnalyzedMaterial,
    isSelected: Boolean,
    onToggle: () -> Unit,
    onEdit: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (material.isUncertain) MaterialTheme.colorScheme.errorContainer 
                            else MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = isSelected, onCheckedChange = { onToggle() })
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = material.type,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = material.title ?: material.sectionTitle ?: material.topicTitle ?: "Nomsiz",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit")
                }
            }

            if (material.isUncertain) {
                Text(
                    text = "⚠️ Tekshirish kerak (AI noaniq joy topgan)",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            
            val previewText = material.body ?: material.question ?: ""
            Text(
                text = previewText.take(150) + if (previewText.length > 150) "..." else "",
                style = MaterialTheme.typography.bodySmall
            )

            if (material.type == "TEST") {
                Row(modifier = Modifier.padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SuggestionChip(onClick = {}, label = { Text("A: ${material.optionA?.take(10)}") })
                    SuggestionChip(onClick = {}, label = { Text("Ans: ${material.correctAnswer}") })
                }
            }
        }
    }
}
