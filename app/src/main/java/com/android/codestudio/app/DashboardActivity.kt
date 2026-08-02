package com.android.codestudio.app

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class DashboardActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        val btnCreateProject = findViewById<Button>(R.id.btnCreateProject)
        val btnOpenProject = findViewById<Button>(R.id.btnOpenProject)
        val btnCloneRepo = findViewById<Button>(R.id.btnCloneRepo)
        val btnTerminal = findViewById<Button>(R.id.btnTerminal)
        val btnGetStarted = findViewById<Button>(R.id.btnGetStarted)
        val btnPreferences = findViewById<Button>(R.id.btnPreferences)
        val btnIdeConfig = findViewById<Button>(R.id.btnIdeConfig)
        val btnDocumentation = findViewById<Button>(R.id.btnDocumentation)

        btnCreateProject.setOnClickListener {
            Toast.makeText(this, "Create Project (coming soon)", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, EditorActivity::class.java))
        }

        btnOpenProject.setOnClickListener {
            Toast.makeText(this, "Open Project (coming soon)", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, EditorActivity::class.java))
        }

        btnCloneRepo.setOnClickListener {
            Toast.makeText(this, "Clone Repository (coming soon)", Toast.LENGTH_SHORT).show()
        }

        btnTerminal.setOnClickListener {
            Toast.makeText(this, "Terminal (coming soon)", Toast.LENGTH_SHORT).show()
        }

        btnGetStarted.setOnClickListener {
            Toast.makeText(this, "Get Started (coming soon)", Toast.LENGTH_SHORT).show()
        }

        btnPreferences.setOnClickListener {
            Toast.makeText(this, "Preferences (coming soon)", Toast.LENGTH_SHORT).show()
        }

        btnIdeConfig.setOnClickListener {
            Toast.makeText(this, "IDE Configurations (coming soon)", Toast.LENGTH_SHORT).show()
        }

        btnDocumentation.setOnClickListener {
            Toast.makeText(this, "Documentation (coming soon)", Toast.LENGTH_SHORT).show()
        }
    }
}
