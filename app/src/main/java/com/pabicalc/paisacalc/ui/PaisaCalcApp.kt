package com.pabicalc.paisacalc.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.pabicalc.paisacalc.R
import com.pabicalc.paisacalc.data.PaisaRepository
import com.pabicalc.paisacalc.monetization.MonetizationController
import com.pabicalc.paisacalc.ui.emi.EmiCalculatorScreen
import com.pabicalc.paisacalc.ui.gst.GstCalculatorScreen

private enum class AppTab { EMI, GST }

/** Two offline calculators, with tab state retained when switching screens. */
@Composable
fun PaisaCalcApp(
    repository: PaisaRepository,
    monetization: MonetizationController,
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit,
) {
    var selectedTab by rememberSaveable { mutableStateOf(AppTab.EMI) }
    val stateHolder = rememberSaveableStateHolder()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                NavigationBarItem(
                    selected = selectedTab == AppTab.EMI,
                    onClick = { selectedTab = AppTab.EMI },
                    icon = { Text("₹", style = MaterialTheme.typography.titleMedium) },
                    label = { Text(stringResource(R.string.tab_emi)) },
                )
                NavigationBarItem(
                    selected = selectedTab == AppTab.GST,
                    onClick = { selectedTab = AppTab.GST },
                    icon = { Text("%", style = MaterialTheme.typography.titleMedium) },
                    label = { Text(stringResource(R.string.tab_gst)) },
                )
            }
        },
    ) { contentPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding)
                .consumeWindowInsets(contentPadding),
        ) {
            stateHolder.SaveableStateProvider(key = selectedTab.name) {
                when (selectedTab) {
                    AppTab.EMI -> EmiCalculatorScreen(
                        repository = repository,
                        monetization = monetization,
                        isDarkTheme = isDarkTheme,
                        onToggleTheme = onToggleTheme,
                    )
                    AppTab.GST -> GstCalculatorScreen(
                        repository = repository,
                        isDarkTheme = isDarkTheme,
                        onToggleTheme = onToggleTheme,
                    )
                }
            }
        }
    }
}
