package com.android.codestudio.app

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val prefs = getSharedPreferences("codestudio_prefs", MODE_PRIVATE)
        val showWelcome = prefs.getBoolean("show_welcome", true)

        if (showWelcome) {
            startActivity(Intent(this, DashboardActivity::class.java))
        } else {
            val lastWorkspace = prefs.getString("workspace_uri", null)
            if (lastWorkspace != null) {
                val intent = Intent(this, EditorActivity::class.java).apply {
                    putExtra("workspace_uri", lastWorkspace)
                }
                startActivity(intent)
            } else {
                startActivity(Intent(this, DashboardActivity::class.java))
            }
        }
        finish()
    }
}
