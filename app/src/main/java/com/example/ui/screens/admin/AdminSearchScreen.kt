package com.example.ui.screens.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.ui.admin.AdminViewModel
import com.example.util.admin.AnalyzedMaterial

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminSearchScreen(
    viewModel: AdminViewModel,
    onNavigateBack: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    // In a real app, I'd search the DB. For prototype, I'll show a placeholder
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Material Qidirish") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                placeholder = { Text("Mavzu yoki matn bo'yicha qidirish...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) }
            )

            Text(
                text = "Tez orada: To'liq qidiruv va tahrirlash imkoniyati",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}
