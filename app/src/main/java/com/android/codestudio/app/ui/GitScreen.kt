package com.android.codestudio.app.ui

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.codestudio.app.utils.CommitInfo
import com.android.codestudio.app.utils.GitManager
import kotlinx.coroutines.launch
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GitScreen(repoPath: String?) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // States
    var currentBranch by remember { mutableStateOf("") }
    var branches by remember { mutableStateOf(listOf<String>()) }
    var status by remember { mutableStateOf(GitManager.GitStatus()) }
    var stagedFiles by remember { mutableStateOf(listOf<String>()) }
    var commitMessage by remember { mutableStateOf("") }
    var commitHistory by remember { mutableStateOf(listOf<CommitInfo>()) }
    var remoteOrigin by remember { mutableStateOf("") }
    var latestCommit by remember { mutableStateOf<CommitInfo?>(null) }

    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    // Load Git data
    LaunchedEffect(repoPath) {
        if (repoPath.isNullOrEmpty()) {
            isLoading = false
            errorMessage = "No repository open"
            return@LaunchedEffect
        }
        val path = File(repoPath)
        if (!GitManager.isGitRepo(path)) {
            isLoading = false
            errorMessage = "Not a Git repository"
            return@LaunchedEffect
        }

        isLoading = true
        errorMessage = null
        try {
            val branchResult = GitManager.getCurrentBranch(path)
            if (branchResult.isSuccess) currentBranch = branchResult.getOrNull() ?: ""
            else errorMessage = branchResult.exceptionOrNull()?.message

            val branchesResult = GitManager.getBranches(path)
            if (branchesResult.isSuccess) branches = branchesResult.getOrNull() ?: emptyList()

            val statusResult = GitManager.getStatus(path)
            if (statusResult.isSuccess) status = statusResult.getOrNull() ?: GitManager.GitStatus()
            else errorMessage = statusResult.exceptionOrNull()?.message

            val logResult = GitManager.getCommitHistory(path, 20)
            if (logResult.isSuccess) commitHistory = logResult.getOrNull() ?: emptyList()

            val originResult = GitManager.getRemoteOrigin(path)
            if (originResult.isSuccess) remoteOrigin = originResult.getOrNull() ?: "No remote"

            val latestResult = GitManager.getLatestCommit(path)
            if (latestResult.isSuccess) latestCommit = latestResult.getOrNull()

            isLoading = false
        } catch (e: Exception) {
            isLoading = false
            errorMessage = e.message
        }
    }

    // Helper to refresh data
    fun refreshData() {
        val path = File(repoPath ?: "")
        coroutineScope.launch {
            val statusResult = GitManager.getStatus(path)
            if (statusResult.isSuccess) status = statusResult.getOrNull() ?: GitManager.GitStatus()
            val logResult = GitManager.getCommitHistory(path, 20)
            if (logResult.isSuccess) commitHistory = logResult.getOrNull() ?: emptyList()
            val latestResult = GitManager.getLatestCommit(path)
            if (latestResult.isSuccess) latestCommit = latestResult.getOrNull()
        }
    }

    if (repoPath.isNullOrEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No repository open. Please open a project from the Repositories tab.")
        }
        return
    }

    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    if (errorMessage != null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Error: $errorMessage", color = MaterialTheme.colorScheme.error)
        }
        return
    }

    val surfaceColor = MaterialTheme.colorScheme.surface
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    val primaryColor = MaterialTheme.colorScheme.primary

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // ------------------ Latest Commit Status ------------------
        Text("Latest Commit", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = onSurfaceColor)
        if (latestCommit != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = surfaceColor),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Commit: ${latestCommit!!.id.take(8)}",
                        color = primaryColor,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = latestCommit!!.shortMessage,
                        color = onSurfaceColor,
                        fontSize = 16.sp
                    )
                    Row {
                        Text(
                            text = "Author: ${latestCommit!!.authorName} <${latestCommit!!.authorEmail}>",
                            color = onSurfaceColor.copy(alpha = 0.7f),
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Text(
                            text = latestCommit!!.date,
                            color = onSurfaceColor.copy(alpha = 0.5f),
                            fontSize = 14.sp
                        )
                    }
                }
            }
        } else {
            Text("No commits yet", color = onSurfaceColor.copy(alpha = 0.6f))
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ------------------ Branch selector + Remote ------------------
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            var expanded by remember { mutableStateOf(false) }
            Box {
                OutlinedButton(
                    onClick = { expanded = true },
                    shape = RoundedCornerShape(50)
                ) {
                    Icon(Icons.Filled.ForkRight, contentDescription = null)
                    Text(" $currentBranch", modifier = Modifier.padding(start = 4.dp))
                }
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    branches.forEach { branch ->
                        DropdownMenuItem(
                            text = { Text(branch) },
                            onClick = {
                                expanded = false
                                coroutineScope.launch {
                                    val result = GitManager.switchBranch(File(repoPath), branch)
                                    if (result.isSuccess) {
                                        Toast.makeText(context, "Switched to $branch", Toast.LENGTH_SHORT).show()
                                        currentBranch = branch
                                        refreshData()
                                    } else {
                                        Toast.makeText(context, "Failed: ${result.exceptionOrNull()?.message}", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        )
                    }
                }
            }
            Text(
                text = remoteOrigin.take(30),
                color = onSurfaceColor.copy(alpha = 0.6f),
                fontSize = 12.sp
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ------------------ Action buttons ------------------
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = {
                    coroutineScope.launch {
                        val result = GitManager.fetch(File(repoPath), username, password)
                        Toast.makeText(context, if (result.isSuccess) "Fetch successful" else "Fetch failed: ${result.exceptionOrNull()?.message}", Toast.LENGTH_SHORT).show()
                        if (result.isSuccess) refreshData()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(50)
            ) {
                Icon(Icons.Filled.Download, contentDescription = null)
                Text("Fetch", modifier = Modifier.padding(start = 4.dp))
            }
            Button(
                onClick = {
                    coroutineScope.launch {
                        val result = GitManager.pull(File(repoPath), username, password)
                        Toast.makeText(context, if (result.isSuccess) "Pull successful" else "Pull failed: ${result.exceptionOrNull()?.message}", Toast.LENGTH_SHORT).show()
                        if (result.isSuccess) refreshData()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(50)
            ) {
                Icon(Icons.Filled.GetApp, contentDescription = null)
                Text("Pull", modifier = Modifier.padding(start = 4.dp))
            }
            Button(
                onClick = {
                    coroutineScope.launch {
                        val result = GitManager.push(File(repoPath), username, password)
                        Toast.makeText(context, if (result.isSuccess) "Push successful" else "Push failed: ${result.exceptionOrNull()?.message}", Toast.LENGTH_SHORT).show()
                        if (result.isSuccess) refreshData()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(50)
            ) {
                Icon(Icons.Filled.Send, contentDescription = null)
                Text("Push", modifier = Modifier.padding(start = 4.dp))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ------------------ Changes ------------------
        Text("Changes", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = onSurfaceColor)
        LazyColumn(
            modifier = Modifier.height(200.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            val allFiles = status.modified + status.added + status.deleted + status.untracked + status.conflicting
            items(allFiles) { file ->
                val isStaged = stagedFiles.contains(file)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            if (isStaged) stagedFiles = stagedFiles - file
                            else stagedFiles = stagedFiles + file
                        }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = isStaged,
                        onCheckedChange = { checked ->
                            if (checked) stagedFiles = stagedFiles + file
                            else stagedFiles = stagedFiles - file
                        }
                    )
                    Text(
                        text = file,
                        color = onSurfaceColor,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    when {
                        status.modified.contains(file) -> Text("modified", color = Color(0xFFFFA726), fontSize = 12.sp)
                        status.added.contains(file) -> Text("added", color = Color(0xFF66BB6A), fontSize = 12.sp)
                        status.deleted.contains(file) -> Text("deleted", color = Color(0xFFEF5350), fontSize = 12.sp)
                        status.untracked.contains(file) -> Text("untracked", color = Color(0xFF42A5F5), fontSize = 12.sp)
                        status.conflicting.contains(file) -> Text("conflict", color = Color(0xFFAB47BC), fontSize = 12.sp)
                    }
                }
            }
        }

        // Stage/Unstage all
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = {
                    coroutineScope.launch {
                        val allFilesList = status.modified + status.added + status.deleted + status.untracked + status.conflicting
                        val result = GitManager.stageFiles(File(repoPath), allFilesList)
                        if (result.isSuccess) {
                            Toast.makeText(context, "Staged all files", Toast.LENGTH_SHORT).show()
                            stagedFiles = allFilesList
                            refreshData()
                        } else {
                            Toast.makeText(context, "Stage failed: ${result.exceptionOrNull()?.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(50)
            ) {
                Text("Stage All")
            }
            Button(
                onClick = {
                    coroutineScope.launch {
                        val result = GitManager.unstageFiles(File(repoPath), stagedFiles)
                        if (result.isSuccess) {
                            Toast.makeText(context, "Unstaged all files", Toast.LENGTH_SHORT).show()
                            stagedFiles = emptyList()
                            refreshData()
                        } else {
                            Toast.makeText(context, "Unstage failed: ${result.exceptionOrNull()?.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(50)
            ) {
                Text("Unstage All")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ------------------ Commit ------------------
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = commitMessage,
                onValueChange = { commitMessage = it },
                label = { Text("Commit message") },
                modifier = Modifier.weight(1f),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors()
            )
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = {
                    if (stagedFiles.isEmpty()) {
                        Toast.makeText(context, "No files staged", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    if (commitMessage.isBlank()) {
                        Toast.makeText(context, "Please enter a commit message", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    coroutineScope.launch {
                        val result = GitManager.commit(File(repoPath), commitMessage)
                        if (result.isSuccess) {
                            val commitInfo = result.getOrNull()
                            Toast.makeText(context, "Committed: ${commitInfo?.id?.take(8)}", Toast.LENGTH_SHORT).show()
                            commitMessage = ""
                            stagedFiles = emptyList()
                            refreshData()
                        } else {
                            Toast.makeText(context, "Commit failed: ${result.exceptionOrNull()?.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                shape = RoundedCornerShape(50)
            ) {
                Text("Commit")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ------------------ Commit History ------------------
        Text("History", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = onSurfaceColor)
        LazyColumn(
            modifier = Modifier.height(150.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(commitHistory) { commit ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "${commit.id.take(8)} ${commit.shortMessage}",
                            color = onSurfaceColor,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "${commit.authorName} • ${commit.date}",
                            color = onSurfaceColor.copy(alpha = 0.6f),
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ------------------ Authentication (optional) ------------------
        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Username (optional)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors()
        )
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password / Token (optional)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors()
        )
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password / Token (optional)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors()
        )
    }
}
