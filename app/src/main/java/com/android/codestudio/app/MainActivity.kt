package com.android.codestudio.app

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Check if the user has completed setup
        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        val isSetupComplete = prefs.getBoolean("setup_complete", false)

        if (isSetupComplete) {
            // Go straight to the editor
            startActivity(Intent(this, EditorActivity::class.java))
        } else {
            // Show the setup wizard
            startActivity(Intent(this, SetupActivity::class.java))
        }
        finish() // close MainActivity so user can't press back to it
    }
}
