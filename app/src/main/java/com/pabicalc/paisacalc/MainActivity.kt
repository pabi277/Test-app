package com.pabicalc.paisacalc

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.pabicalc.paisacalc.ui.emi.EmiCalculatorScreen
import com.pabicalc.paisacalc.ui.theme.PaisaCalcTheme

/**
 * Single-activity Compose host. Phase 1 replaces the bootstrap hello screen
 * with the offline EMI calculator (engine + ₹ formatting + amortization
 * schedule); tabbed navigation arrives in phase 3.
 */
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PaisaCalcTheme {
                EmiCalculatorScreen()
            }
        }
    }
}
