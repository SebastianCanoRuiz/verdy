package com.verdy.presentation.screen.plants.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Grass
import androidx.compose.material.icons.outlined.MeetingRoom
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.verdy.R
import com.verdy.domain.model.PlantEnvironment
import com.verdy.presentation.component.EmptyState
import com.verdy.presentation.component.EnvironmentSectionHeader
import com.verdy.presentation.component.PlantCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlantListScreen(
    onPlantClick: (Long) -> Unit,
    onAddPlant: () -> Unit,
    viewModel: PlantListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.deletedPlantName) {
        uiState.deletedPlantName?.let {
            snackbarHostState.showSnackbar("$it eliminada")
            viewModel.clearDeletedMessage()
        }
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    if (uiState.showManageEnvironmentsDialog) {
        ManageEnvironmentsDialog(
            environments = uiState.environments,
            onDismiss = viewModel::dismissManageEnvironments,
            onRename = viewModel::renameEnvironment,
            onDelete = viewModel::deleteEnvironment
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mis plantas") },
                actions = {
                    IconButton(onClick = viewModel::showManageEnvironments) {
                        Icon(Icons.Outlined.MeetingRoom, contentDescription = stringResource(R.string.environment_manage))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddPlant,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Agregar planta")
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = viewModel::onSearchQueryChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text("Buscar planta…") },
                leadingIcon = {
                    Icon(Icons.Outlined.Search, contentDescription = null)
                },
                shape = RoundedCornerShape(16.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = MaterialTheme.colorScheme.surfaceVariant,
                    focusedBorderColor = MaterialTheme.colorScheme.primary
                )
            )

            when {
                uiState.isLoading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                uiState.plants.isEmpty() -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        EmptyState(
                            icon = Icons.Outlined.Grass,
                            message = "Aún no tienes plantas",
                            subtitle = "Toca + para agregar tu primera planta"
                        )
                    }
                }
                uiState.groups.isEmpty() -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        EmptyState(
                            icon = Icons.Outlined.Search,
                            message = "Sin resultados",
                            subtitle = "Intenta con otro nombre"
                        )
                    }
                }
                else -> {
                    LazyColumn(
                        contentPadding = PaddingValues(
                            start = 16.dp, end = 16.dp, bottom = 88.dp, top = 8.dp
                        ),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        uiState.groups.forEach { group ->
                            item(key = "header_${group.environment?.id ?: "none"}") {
                                EnvironmentSectionHeader(
                                    environmentName = group.environment?.name
                                        ?: stringResource(R.string.environment_unassigned),
                                    plantCount = group.plants.size
                                )
                            }
                            group.plants.chunked(2).forEachIndexed { rowIndex, rowPlants ->
                                item(key = "row_${group.environment?.id ?: "none"}_$rowIndex") {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        rowPlants.forEach { plant ->
                                            PlantCard(
                                                plant = plant,
                                                environmentName = viewModel.environmentNameFor(plant),
                                                onClick = { onPlantClick(plant.id) },
                                                modifier = Modifier.weight(1f)
                                            )
                                        }
                                        if (rowPlants.size == 1) {
                                            Box(modifier = Modifier.weight(1f))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ManageEnvironmentsDialog(
    environments: List<PlantEnvironment>,
    onDismiss: () -> Unit,
    onRename: (Long, String) -> Unit,
    onDelete: (Long) -> Unit
) {
    var editingEnv by remember { mutableStateOf<PlantEnvironment?>(null) }
    var editName by remember { mutableStateOf("") }
    var deletingEnv by remember { mutableStateOf<PlantEnvironment?>(null) }

    editingEnv?.let { env ->
        AlertDialog(
            onDismissRequest = { editingEnv = null },
            title = { Text(stringResource(R.string.environment_rename)) },
            text = {
                OutlinedTextField(
                    value = editName,
                    onValueChange = { editName = it },
                    label = { Text(stringResource(R.string.environment_create_hint)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onRename(env.id, editName)
                        editingEnv = null
                    },
                    enabled = editName.isNotBlank()
                ) { Text(stringResource(R.string.save)) }
            },
            dismissButton = {
                TextButton(onClick = { editingEnv = null }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    deletingEnv?.let { env ->
        AlertDialog(
            onDismissRequest = { deletingEnv = null },
            title = { Text(stringResource(R.string.environment_delete)) },
            text = { Text(stringResource(R.string.environment_delete_confirm, env.name)) },
            confirmButton = {
                TextButton(onClick = {
                    onDelete(env.id)
                    deletingEnv = null
                }) { Text(stringResource(R.string.delete), color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { deletingEnv = null }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    if (editingEnv == null && deletingEnv == null) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text(stringResource(R.string.environment_manage)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    environments.forEach { env ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(env.name, modifier = Modifier.weight(1f))
                            TextButton(onClick = {
                                editingEnv = env
                                editName = env.name
                            }) { Text(stringResource(R.string.environment_rename)) }
                            TextButton(onClick = { deletingEnv = env }) {
                                Text(stringResource(R.string.delete), color = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) }
            }
        )
    }
}
