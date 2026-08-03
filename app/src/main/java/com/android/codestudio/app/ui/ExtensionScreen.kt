package com.android.codestudio.app.ui

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.codestudio.app.data.Extension
import com.android.codestudio.app.data.PreferencesManager

@Composable
fun ExtensionsScreen(
    extensions: List<Extension>,
    onExtensionStateChanged: (List<Extension>) -> Unit
) {
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("codestudio_prefs", android.content.Context.MODE_PRIVATE)
    var extList by remember { mutableStateOf(extensions) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(extList) { ext ->
            ExtensionItem(
                ext = ext,
                onToggle = {
                    // Toggle state
                    val updatedList = extList.map {
                        if (it.name == ext.name) it.copy(installed = !it.installed) else it
                    }
                    extList = updatedList
                    // Save to SharedPreferences
                    PreferencesManager.saveExtensions(prefs, updatedList)
                    // Notify parent (optional)
                    onExtensionStateChanged(updatedList)
                    Toast.makeText(
                        context,
                        if (ext.installed) "Uninstalled ${ext.name}" else "Installed ${ext.name}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            )
        }
    }
}

@Composable
fun ExtensionItem(
    ext: Extension,
    onToggle: () -> Unit
) {
    val surfaceColor = MaterialTheme.colorScheme.surface
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    val primaryColor = MaterialTheme.colorScheme.primary

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = surfaceColor),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = ext.name, color = onSurfaceColor, fontWeight = FontWeight.Bold)
                Text(text = ext.description, color = onSurfaceColor.copy(alpha = 0.7f), fontSize = 14.sp)
            }
            if (ext.installed) {
                OutlinedButton(
                    onClick = onToggle,
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = primaryColor
                    )
                ) {
                    Icon(Icons.Filled.Check, contentDescription = null, tint = primaryColor)
                    Text("Installed", color = primaryColor, modifier = Modifier.padding(start = 4.dp))
                }
            } else {
                Button(
                    onClick = onToggle,
                    colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                    shape = RoundedCornerShape(50)
                ) {
                    Text("Install")
                }
            }
        }
    }
}
