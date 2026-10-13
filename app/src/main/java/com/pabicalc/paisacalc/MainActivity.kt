package com.pabicalc.paisacalc

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.SideEffect
import androidx.core.view.WindowInsetsControllerCompat
import com.pabicalc.paisacalc.data.PaisaDatabase
import com.pabicalc.paisacalc.data.PaisaRepository
import com.pabicalc.paisacalc.monetization.MonetizationController
import com.pabicalc.paisacalc.ui.PaisaCalcApp
import com.pabicalc.paisacalc.ui.theme.PaisaCalcTheme

/** Single-activity host for the offline EMI and GST calculators. */
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val preferences = remember {
                getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
            }
            val systemDarkTheme = isSystemInDarkTheme()
            var darkTheme by rememberSaveable {
                mutableStateOf(preferences.getBoolean(KEY_DARK_THEME, systemDarkTheme))
            }
            val repository = remember {
                PaisaRepository(PaisaDatabase.getInstance(applicationContext))
            }
            val monetization = remember { MonetizationController(applicationContext) }

            // UMP asks for a consent-information update on every launch. It is
            // the only monetization work that happens before the user acts: the
            // ads SDK is not initialized and Play billing is not connected.
            LaunchedEffect(Unit) {
                monetization.gatherConsent(this@MainActivity)
            }
            DisposableEffect(Unit) {
                onDispose { monetization.shutdown() }
            }

            LaunchedEffect(darkTheme) {
                preferences.edit().putBoolean(KEY_DARK_THEME, darkTheme).apply()
            }
            SideEffect {
                WindowInsetsControllerCompat(window, window.decorView).apply {
                    isAppearanceLightStatusBars = !darkTheme
                    isAppearanceLightNavigationBars = !darkTheme
                }
            }

            PaisaCalcTheme(darkTheme = darkTheme) {
                PaisaCalcApp(
                    repository = repository,
                    monetization = monetization,
                    isDarkTheme = darkTheme,
                    onToggleTheme = { darkTheme = !darkTheme },
                )
            }
        }
    }

    private companion object {
        const val PREFERENCES_NAME = "paisa_calc_settings"
        const val KEY_DARK_THEME = "dark_theme"
    }
}
