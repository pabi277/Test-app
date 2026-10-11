package com.pabicalc.paisacalc

import android.app.Activity
import android.os.Bundle

/**
 * Phase 0 bootstrap screen: hello world only.
 * The EMI engine and calculator UI land in phase 1.
 */
class MainActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }
}
