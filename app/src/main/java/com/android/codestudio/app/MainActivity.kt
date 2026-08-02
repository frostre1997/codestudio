package com.android.codestudio.app

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val prefs = getSharedPreferences("codestudio_prefs", MODE_PRIVATE)
        val isSetupComplete = prefs.getBoolean("setup_complete", false)

        if (isSetupComplete) {
            startActivity(Intent(this, EditorActivity::class.java))
        } else {
            startActivity(Intent(this, SetupActivity::class.java))
        }
        finish()
    }
}
