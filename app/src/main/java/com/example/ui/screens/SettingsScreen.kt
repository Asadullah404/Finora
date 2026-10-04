package com.example.ui.screens

import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CurrencyExchange
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.calculation.FinancialEngine
import com.example.model.UserProfile
import com.example.ui.FinoraViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.auth

@Composable
fun SettingsScreen(
    viewModel: FinoraViewModel,
    onNavigateToCategories: () -> Unit,
    onSignOut: () -> Unit,
    modifier: Modifier = Modifier
) {
    val profile by viewModel.userProfile.collectAsState()
    val authUser = Firebase.auth.currentUser

    var showCurrencyMenu by remember { mutableStateOf(false) }
    var showOpeningBalanceDialog by remember { mutableStateOf(false) }
    var showWarningThresholdDialog by remember { mutableStateOf(false) }

    val currencyList = listOf("PKR", "USD", "EUR", "GBP", "SAR", "AED", "CAD", "AUD", "INR")

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("settings_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Settings & Preferences",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        // Account Profile Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            modifier = Modifier.size(48.dp),
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.Person,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = authUser?.displayName ?: "Finora Member",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = authUser?.email ?: "Signed in via Google",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = onSignOut,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .testTag("sign_out_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null, tint = Color(0xFFF43F5E), modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Sign Out", color = Color(0xFFF43F5E), fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }

        // Financial Preferences Card
        item {
            Text(
                text = "Financial Preferences",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column {
                    // Currency Selection
                    SettingsItem(
                        icon = Icons.Default.CurrencyExchange,
                        title = "Base Currency",
                        subtitle = "Current: ${profile.currencyCode}",
                        onClick = { showCurrencyMenu = true }
                    ) {
                        DropdownMenu(
                            expanded = showCurrencyMenu,
                            onDismissRequest = { showCurrencyMenu = false }
                        ) {
                            currencyList.forEach { cur ->
                                DropdownMenuItem(
                                    text = { Text(cur, fontWeight = if (cur == profile.currencyCode) FontWeight.Bold else FontWeight.Normal) },
                                    onClick = {
                                        viewModel.saveUserProfile(profile.copy(currencyCode = cur))
                                        showCurrencyMenu = false
                                    }
                                )
                            }
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                    // Opening / Starting Balance
                    SettingsItem(
                        icon = Icons.Default.AccountBalance,
                        title = "Historical Starting Balance",
                        subtitle = "Opening balance: ${FinancialEngine.formatMinorUnits(profile.openingBalanceMinorUnits, profile.currencyCode)}",
                        onClick = { showOpeningBalanceDialog = true }
                    )

                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                    // Categories Management
                    SettingsItem(
                        icon = Icons.Default.Category,
                        title = "Manage Categories",
                        subtitle = "Customize income and expense categories",
                        onClick = onNavigateToCategories
                    )

                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                    // Budget Warning Threshold
                    SettingsItem(
                        icon = Icons.Default.NotificationsActive,
                        title = "Budget Alert Threshold",
                        subtitle = "Alert when spending reaches ${profile.budgetWarningPercent}%",
                        onClick = { showWarningThresholdDialog = true }
                    )

                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                    // Privacy Mode (Hide Balances)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Icon(Icons.Default.Visibility, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(14.dp))
                            Column {
                                Text("Hide Balances", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                                Text("Conceal balances on dashboard", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        Switch(
                            checked = profile.hideBalance,
                            onCheckedChange = { viewModel.toggleHideBalance() },
                            colors = SwitchDefaults.colors(checkedThumbColor = MaterialTheme.colorScheme.primary)
                        )
                    }
                }
            }
        }

        // Security & Cloud Architecture
        item {
            Text(
                text = "Cloud Persistence & Security",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CloudDone, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("Firebase Cloud Firestore: Active", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                    }
                    Text(
                        text = "Real-time bi-directional database synchronization with automatic local offline caching. Transactions work offline and sync immediately upon reconnection.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Security, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("Zero-Trust Multi-Tenant Isolation", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                    }
                    Text(
                        text = "All database records are authenticated via Google Identity with strict per-user Firestore security rules. No unauthorized reads or cross-user writes are permitted.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }

    if (showOpeningBalanceDialog) {
        var balInput by remember {
            mutableStateOf(
                if (profile.openingBalanceMinorUnits != 0L) {
                    (profile.openingBalanceMinorUnits / 100L).toString()
                } else ""
            )
        }
        AlertDialog(
            onDismissRequest = { showOpeningBalanceDialog = false },
            title = { Text("Starting Balance", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text(
                        "Set your initial balance before using Finora. This starting balance is carried forward into all cumulative balance calculations.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = balInput,
                        onValueChange = { balInput = it.filter { ch -> ch.isDigit() || ch == '-' } },
                        label = { Text("Opening Balance (${profile.currencyCode})") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val minor = FinancialEngine.parseToMinorUnits(balInput)
                        viewModel.saveUserProfile(profile.copy(openingBalanceMinorUnits = minor))
                        showOpeningBalanceDialog = false
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showOpeningBalanceDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showWarningThresholdDialog) {
        var threshold by remember { mutableIntStateOf(profile.budgetWarningPercent) }
        AlertDialog(
            onDismissRequest = { showWarningThresholdDialog = false },
            title = { Text("Budget Warning Threshold", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text(
                        "Show warning notifications on budget cards when spending reaches $threshold% of the category limit.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Slider(
                        value = threshold.toFloat(),
                        onValueChange = { threshold = it.toInt() },
                        valueRange = 50f..95f,
                        steps = 8
                    )
                    Text("$threshold%", fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.CenterHorizontally))
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.saveUserProfile(profile.copy(budgetWarningPercent = threshold))
                        showWarningThresholdDialog = false
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showWarningThresholdDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun SettingsItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    trailingContent: @Composable () -> Unit = {
        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
    }
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.width(14.dp))
            Column {
                Text(title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        trailingContent()
    }
}
