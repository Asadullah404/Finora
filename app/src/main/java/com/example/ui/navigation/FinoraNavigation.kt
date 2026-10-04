package com.example.ui.navigation

import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import com.example.ui.FinoraViewModel
import com.example.ui.screens.BudgetsScreen
import com.example.ui.screens.CategoriesScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.ReportsScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.performSignOut

enum class FinoraNavSection(
    val title: String,
    val icon: ImageVector,
    val testTag: String
) {
    HOME("Home", Icons.Default.Home, "nav_home"),
    TRANSACTIONS("Transactions", Icons.Default.ReceiptLong, "nav_transactions"),
    REPORTS("Reports", Icons.Default.BarChart, "nav_reports"),
    BUDGETS("Budgets", Icons.Default.PieChart, "nav_budgets"),
    SETTINGS("Settings", Icons.Default.Settings, "nav_settings")
}

@Composable
fun FinoraAppContent(
    viewModel: FinoraViewModel,
    onSignOut: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember { CredentialManager.create(context) }

    var currentSection by remember { mutableStateOf(FinoraNavSection.HOME) }
    var inSubScreen by remember { mutableStateOf<String?>(null) } // "categories"

    if (inSubScreen == "categories") {
        BackHandler { inSubScreen = null }
        CategoriesScreen(
            viewModel = viewModel,
            onBack = { inSubScreen = null }
        )
        return
    }

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                FinoraNavSection.values().forEach { section ->
                    val isSelected = currentSection == section
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { currentSection = section },
                        icon = {
                            Icon(
                                imageVector = section.icon,
                                contentDescription = section.title,
                                modifier = Modifier.size(22.dp)
                            )
                        },
                        label = { Text(section.title) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.testTag(section.testTag)
                    )
                }
            }
        },
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            when (currentSection) {
                FinoraNavSection.HOME -> {
                    DashboardScreen(
                        viewModel = viewModel,
                        onNavigateToTransactions = { currentSection = FinoraNavSection.TRANSACTIONS }
                    )
                }
                FinoraNavSection.TRANSACTIONS -> {
                    com.example.ui.screens.TransactionsScreen(viewModel = viewModel)
                }
                FinoraNavSection.REPORTS -> {
                    ReportsScreen(viewModel = viewModel)
                }
                FinoraNavSection.BUDGETS -> {
                    BudgetsScreen(viewModel = viewModel)
                }
                FinoraNavSection.SETTINGS -> {
                    SettingsScreen(
                        viewModel = viewModel,
                        onNavigateToCategories = { inSubScreen = "categories" },
                        onSignOut = {
                            performSignOut(
                                context = context,
                                credentialManager = credentialManager,
                                onSignOutComplete = onSignOut,
                                scope = scope
                            )
                        }
                    )
                }
            }
        }
    }
}
