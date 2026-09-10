package com.example.ui.screens.admin

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.ui.admin.AdminViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    viewModel: AdminViewModel,
    onNavigateToImport: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToSearch: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val stats by viewModel.dashboardStats.collectAsState()
    var showBackupDialog by remember { mutableStateOf(false) }

    val createBackupLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) {
            viewModel.createBackup(context, uri)
        }
    }

    val restoreBackupLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            viewModel.restoreBackup(context, uri)
        }
    }

    if (showBackupDialog) {
        AlertDialog(
            onDismissRequest = { showBackupDialog = false },
            title = { Text("Zaxira nusxa (Backup)") },
            text = { Text("Zaxira nusxani yuklab olish (Export) yoki mavjud zaxiradan tiklash (Restore) amalini tanlang.") },
            confirmButton = {
                Button(onClick = {
                    showBackupDialog = false
                    createBackupLauncher.launch("math_master_backup_${System.currentTimeMillis()}.json")
                }) {
                    Text("Eksport (Zaxiralash)")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = {
                    showBackupDialog = false
                    restoreBackupLauncher.launch(arrayOf("application/json", "*/*"))
                }) {
                    Text("Tiklash (Restore)")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Admin Dashboard") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.loadStats() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
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
        ) {
            // Stats Grid
            Text(
                text = "Tizim Statistikasi",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.weight(1f)
            ) {
                item { StatCard("Foydalanuvchilar", stats["users"] ?: 0, Icons.Default.People) }
                item { StatCard("Ordinary Masalalar", stats["ordinary_problems"] ?: 0, Icons.Default.Functions) }
                item { StatCard("MAXSUS Materiallar", stats["maxsus_materials"] ?: 0, Icons.Default.AutoStories) }
                item { StatCard("Mavzular", stats["topics"] ?: 0, Icons.Default.List) }
                item { StatCard("Testlar", stats["tests"] ?: 0, Icons.Default.Quiz) }
                item { StatCard("Importlar", stats["imports"] ?: 0, Icons.Default.History) }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Actions
            Text(
                text = "Boshqaruv",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                AdminActionButton("PDF / TEXT YUKLASH", Icons.Default.FileUpload, onNavigateToImport)
                AdminActionButton("QIDIRUV VA TAHRIR", Icons.Default.Search, onNavigateToSearch)
                AdminActionButton("IMPORT TARIXI", Icons.Default.History, onNavigateToHistory)
                AdminActionButton("ZAXIRA NUSXA (BACKUP)", Icons.Default.Backup) {
                    showBackupDialog = true
                }
            }
        }
    }
}

@Composable
fun StatCard(label: String, value: Int, icon: ImageVector) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = value.toString(), style = MaterialTheme.typography.headlineMedium)
            Text(text = label, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
fun AdminActionButton(label: String, icon: ImageVector, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(16.dp),
        shape = MaterialTheme.shapes.medium
    ) {
        Icon(icon, contentDescription = null)
        Spacer(modifier = Modifier.width(12.dp))
        Text(text = label)
    }
}
