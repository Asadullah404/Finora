package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Category
import com.example.ui.FinoraViewModel
import com.example.ui.components.CategoryIconHelper

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CategoriesScreen(
    viewModel: FinoraViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val categories by viewModel.categories.collectAsState()
    var showDialog by remember { mutableStateOf(false) }
    var editingCategory by remember { mutableStateOf<Category?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Categories", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    editingCategory = null
                    showDialog = true
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("add_category_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Category")
            }
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                Text(
                    text = "Manage your income and expense categories. You can create custom categories, assign icons, and configure colors.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            items(categories, key = { it.id }) { cat ->
                val catColor = try {
                    Color(android.graphics.Color.parseColor(cat.colorHex))
                } catch (_: Exception) {
                    MaterialTheme.colorScheme.primary
                }

                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(catColor.copy(alpha = 0.15f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = CategoryIconHelper.getIcon(cat.icon),
                                contentDescription = null,
                                tint = catColor,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = cat.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Type: ${cat.type.capitalize(java.util.Locale.ROOT)}" + if (cat.isDefault) " • Default" else " • Custom",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        if (!cat.isDefault) {
                            IconButton(onClick = {
                                editingCategory = cat
                                showDialog = true
                            }) {
                                Icon(Icons.Default.Edit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                            }
                            IconButton(onClick = { viewModel.deleteCategory(cat.id) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFF43F5E), modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }

    if (showDialog) {
        CategoryEditDialog(
            category = editingCategory,
            onDismiss = {
                showDialog = false
                editingCategory = null
            },
            onSave = { cat ->
                if (editingCategory == null) {
                    viewModel.addCategory(cat)
                } else {
                    viewModel.updateCategory(cat)
                }
                showDialog = false
                editingCategory = null
            }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CategoryEditDialog(
    category: Category?,
    onDismiss: () -> Unit,
    onSave: (Category) -> Unit
) {
    var name by remember { mutableStateOf(category?.name ?: "") }
    var type by remember { mutableStateOf(category?.type ?: "expense") }
    var selectedIcon by remember { mutableStateOf(category?.icon ?: "category") }
    var selectedColor by remember { mutableStateOf(category?.colorHex ?: "#10B981") }

    val iconOptions = listOf(
        "restaurant", "shopping_bag", "directions_car", "two_wheeler", "home",
        "bolt", "medical_services", "school", "sports_esports", "subscriptions",
        "spa", "flight", "payments", "military_tech", "laptop_mac", "store",
        "currency_exchange", "savings", "more_horiz"
    )

    val colorOptions = listOf(
        "#10B981", "#3B82F6", "#6366F1", "#8B5CF6", "#EC4899",
        "#F43F5E", "#F59E0B", "#14B8A6", "#06B6D4", "#64748B"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(if (category == null) "New Category" else "Edit Category", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Category Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text("Category Type", style = MaterialTheme.typography.labelSmall)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("expense", "income", "both").forEach { t ->
                        val isSel = type == t
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                            onClick = { type = t },
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(vertical = 6.dp)) {
                                Text(
                                    t.capitalize(java.util.Locale.ROOT),
                                    fontSize = 12.sp,
                                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSel) Color.White else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text("Icon", style = MaterialTheme.typography.labelSmall)
                FlowRow(
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    iconOptions.forEach { iconKey ->
                        val isSel = selectedIcon == iconKey
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .background(
                                    if (isSel) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant,
                                    CircleShape
                                )
                                .border(
                                    width = if (isSel) 2.dp else 0.dp,
                                    color = if (isSel) MaterialTheme.colorScheme.primary else Color.Transparent,
                                    shape = CircleShape
                                )
                                .clickable { selectedIcon = iconKey },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = CategoryIconHelper.getIcon(iconKey),
                                contentDescription = null,
                                tint = if (isSel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text("Color", style = MaterialTheme.typography.labelSmall)
                FlowRow(
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    colorOptions.forEach { hex ->
                        val isSel = selectedColor == hex
                        val c = Color(android.graphics.Color.parseColor(hex))
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .background(c, CircleShape)
                                .border(
                                    width = if (isSel) 3.dp else 0.dp,
                                    color = if (isSel) Color.White else Color.Transparent,
                                    shape = CircleShape
                                )
                                .clickable { selectedColor = hex }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        val cat = (category ?: Category()).copy(
                            name = name.trim(),
                            type = type,
                            icon = selectedIcon,
                            colorHex = selectedColor,
                            isDefault = false
                        )
                        onSave(cat)
                    }
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
