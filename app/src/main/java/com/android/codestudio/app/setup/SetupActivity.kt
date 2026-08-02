package com.android.codestudio.app

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class SetupActivity : AppCompatActivity() {

    private var currentStep = 1

    private lateinit var titleTextView: TextView
    private lateinit var descTextView: TextView
    private lateinit var nextButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setup)

        titleTextView = findViewById(R.id.stepTitle)
        descTextView = findViewById(R.id.stepDescription)
        nextButton = findViewById(R.id.nextButton)

        showStep(currentStep)

        nextButton.setOnClickListener {
            if (currentStep == 6) {
                val prefs = getSharedPreferences("codestudio_prefs", MODE_PRIVATE)
                prefs.edit().putBoolean("setup_complete", true).apply()

                startActivity(Intent(this, EditorActivity::class.java))
                finish()
            } else {
                currentStep++
                showStep(currentStep)
            }
        }
    }

    private fun showStep(step: Int) {
        when (step) {
            1 -> {
                titleTextView.text = "Welcome to CodeStudio"
                descTextView.text = "This wizard will help you set up your workspace.\nTap Next to get started."
                nextButton.text = "Next"
            }
            2 -> {
                titleTextView.text = "Step 2: Workspace"
                descTextView.text = "You'll pick a folder for your projects here.\nWe'll add the folder picker in the next step."
                nextButton.text = "Next"
            }
            3 -> {
                titleTextView.text = "Step 3: Appearance"
                descTextView.text = "Choose Dark/Light theme and font size.\nComing soon!"
                nextButton.text = "Next"
            }
            4 -> {
                titleTextView.text = "Step 4: Account"
                descTextView.text = "Connect your GitHub account here.\nLogin flow will be added next."
                nextButton.text = "Next"
            }
            5 -> {
                titleTextView.text = "Step 5: Repo & Branch"
                descTextView.text = "Select your repo and branch for builds.\nWe'll fetch them from GitHub."
                nextButton.text = "Next"
            }
            6 -> {
                titleTextView.text = "Setup Complete! 🎉"
                descTextView.text = "You're all set. Tap 'Start Coding' to open the editor."
                nextButton.text = "Start Coding"
            }
        }
    }
}
