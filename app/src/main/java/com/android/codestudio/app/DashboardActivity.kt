package com.android.codestudio.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class DashboardActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                DashboardScreen(
                    onCreateProject = {
                        startActivity(Intent(this, EditorActivity::class.java))
                    },
                    onOpenProject = {
                        startActivity(Intent(this, EditorActivity::class.java))
                    },
                    onCloneRepo = {
                        // future
                    },
                    onTerminal = {
                        // future
                    },
                    onGetStarted = {
                        // future
                    },
                    onPreferences = {
                        // future
                    },
                    onIdeConfig = {
                        // future
                    },
                    onDocumentation = {
                        // future
                    }
                )
            }
        }
    }
}

@Composable
fun DashboardScreen(
    onCreateProject: () -> Unit,
    onOpenProject: () -> Unit,
    onCloneRepo: () -> Unit,
    onTerminal: () -> Unit,
    onGetStarted: () -> Unit,
    onPreferences: () -> Unit,
    onIdeConfig: () -> Unit,
    onDocumentation: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Android Code Studio",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF3F51B5),
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Text(
            text = "Your Ideas, Anywhere",
            fontSize = 16.sp,
            color = Color.Gray,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        // Grid of buttons (2 columns)
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                DashboardButton("Create project", onClick = onCreateProject, modifier = Modifier.weight(1f))
                DashboardButton("Open existing project", onClick = onOpenProject, modifier = Modifier.weight(1f))
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                DashboardButton("Clone git repository", onClick = onCloneRepo, modifier = Modifier.weight(1f))
                DashboardButton("Terminal", onClick = onTerminal, modifier = Modifier.weight(1f))
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                DashboardButton("Get started", onClick = onGetStarted, modifier = Modifier.weight(1f))
                DashboardButton("Preferences", onClick = onPreferences, modifier = Modifier.weight(1f))
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                DashboardButton("IDE Configurations", onClick = onIdeConfig, modifier = Modifier.weight(1f))
                DashboardButton("Documentation", onClick = onDocumentation, modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun DashboardButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Button(
        onClick = onClick,
        modifier = modifier
            .height(80.dp)
            .padding(4.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black),
        shape = MaterialTheme.shapes.medium,
        elevation = ButtonDefaults.buttonElevation(4.dp)
    ) {
        Text(text = text, fontSize = 14.sp, fontWeight = FontWeight.Medium)
    }
}
