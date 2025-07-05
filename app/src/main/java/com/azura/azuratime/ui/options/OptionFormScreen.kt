package com.azura.azuratime.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.azura.azuratime.viewmodel.OptionsViewModel
import com.azura.azuratime.db.*
import com.azura.azuratime.utils.OptionsHelpers.getName
import com.azura.azuratime.utils.OptionsHelpers.getOrder
import com.azura.azuratime.utils.OptionsHelpers.getParentId
import com.azura.azuratime.utils.OptionsHelpers.setName
import com.azura.azuratime.utils.OptionsHelpers.setOrder
import com.azura.azuratime.utils.OptionsHelpers.setParentId
import com.azura.azuratime.utils.OptionsHelpers.getId
import com.azura.azuratime.ui.components.AzuraInput
import com.azura.azuratime.ui.components.AzuraCard
import com.azura.azuratime.ui.components.AzuraLazyList
import com.azura.azuratime.ui.components.AzuraFormField

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OptionFormScreen(
    type: String,
    viewModel: OptionsViewModel = viewModel(),
    onNavigateBack: () -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var editingOption by remember { mutableStateOf<Any?>(null) }
    
    // Form state
    var name by remember { mutableStateOf("") }
    var displayOrder by remember { mutableStateOf("0") }
    var parentId by remember { mutableStateOf<Int?>(null) }
    
    // Get options based on type
    val options = when (type) {
        "Class" -> viewModel.classOptions.collectAsState(initial = emptyList())
        "SubClass" -> viewModel.subClassOptions.collectAsState(initial = emptyList())
        "Grade" -> viewModel.gradeOptions.collectAsState(initial = emptyList())
        "SubGrade" -> viewModel.subGradeOptions.collectAsState(initial = emptyList())
        "Program" -> viewModel.programOptions.collectAsState(initial = emptyList<Any>())
        "Role" -> viewModel.roleOptions.collectAsState(initial = emptyList<Any>())
        else -> remember { mutableStateOf(emptyList<Any>()) }
    }

    // Get parent options for SubClass and SubGrade
    val parentOptions = when (type) {
        "SubClass" -> viewModel.classOptions.collectAsState(initial = emptyList()).value
        "SubGrade" -> viewModel.gradeOptions.collectAsState(initial = emptyList()).value
        else -> emptyList()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Manage $type Options") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showAddDialog = true }) {
                        Icon(Icons.Default.Add, "Add Option")
                    }
                }
            )
        }
    ) { padding ->
        AzuraLazyList(
            items = options.value,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) { option ->
            OptionCard(
                option = option,
                onEdit = { editingOption = option },
                onDelete = { viewModel.deleteOption(type, option) }
            )
        }
        
        // Add/Edit Dialog
        if (showAddDialog || editingOption != null) {
            AlertDialog(
                onDismissRequest = {
                    showAddDialog = false
                    editingOption = null
                    name = ""
                    displayOrder = "0"
                    parentId = null
                },
                title = { Text(if (editingOption != null) "Edit $type" else "Add New $type") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        AzuraFormField(
                            value = name,
                            onValueChange = { name = it },
                            label = "Name",
                            isError = name.isBlank(),
                            helperText = if (name.isBlank()) "Name is required" else null,
                            modifier = Modifier.fillMaxWidth()
                        )
                        AzuraFormField(
                            value = displayOrder,
                            onValueChange = { displayOrder = it },
                            label = "Display Order",
                            modifier = Modifier.fillMaxWidth()
                        )
                        
                        // Parent selector for SubClass and SubGrade
                        if (type == "SubClass" || type == "SubGrade") {
                            var expanded by remember { mutableStateOf(false) }
                            val parentList = when (type) {
                                "SubClass" -> parentOptions.filterIsInstance<ClassOption>()
                                "SubGrade" -> parentOptions.filterIsInstance<GradeOption>()
                                else -> emptyList()
                            }
                            ExposedDropdownMenuBox(
                                expanded = expanded,
                                onExpandedChange = { expanded = it }
                            ) {
                                OutlinedTextField(
                                    value = parentList.find { parent -> getId(parent) == parentId }?.let { getName(it) } ?: "",
                                    onValueChange = {},
                                    label = { Text("Parent") },
                                    readOnly = true,
                                    modifier = Modifier.fillMaxWidth(),
                                    trailingIcon = {
                                        IconButton(onClick = { expanded = true }) {
                                            Icon(Icons.Default.ArrowDropDown, null)
                                        }
                                    }
                                )
                                ExposedDropdownMenu(
                                    expanded = expanded,
                                    onDismissRequest = { expanded = false }
                                ) {
                                    parentList.forEach {
                                        DropdownMenuItem(
                                            text = { Text(getName(it)) },
                                            onClick = {
                                                parentId = getId(it)
                                                expanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            if (editingOption != null) {
                                viewModel.updateOption(
                                    type,
                                    editingOption!!,
                                    name,
                                    displayOrder.toIntOrNull() ?: 0,
                                    parentId
                                )
                            } else {
                                viewModel.addOption(
                                    type,
                                    name,
                                    displayOrder.toIntOrNull() ?: 0,
                                    parentId
                                )
                            }
                            showAddDialog = false
                            editingOption = null
                            name = ""
                            displayOrder = "0"
                            parentId = null
                        }
                    ) {
                        Text(if (editingOption != null) "Update" else "Add")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            showAddDialog = false
                            editingOption = null
                            name = ""
                            displayOrder = "0"
                            parentId = null
                        }
                    ) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OptionCard(
    option: Any,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    AzuraCard(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = getName(option),
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "Order: ${getOrder(option)}",
                    style = MaterialTheme.typography.bodyMedium
                )
                getParentId(option)?.let { parentId ->
                    Text(
                        text = "Parent ID: $parentId",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
            Row {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, "Edit")
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, "Delete")
                }
            }
        }
    }
}
