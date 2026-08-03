package com.android.codestudio.app.utils

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.lib.Ref
import org.eclipse.jgit.revwalk.RevCommit
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

data class GitStatus(
    val modified: List<String> = emptyList(),
    val added: List<String> = emptyList(),
    val deleted: List<String> = emptyList(),
    val untracked: List<String> = emptyList(),
    val conflicting: List<String> = emptyList()
)

data class CommitInfo(
    val id: String,
    val shortMessage: String,
    val fullMessage: String,
    val authorName: String,
    val authorEmail: String,
    val date: String,
    val timestamp: Long
)

object GitManager {

    suspend fun isGitRepo(repoPath: File): Boolean = withContext(Dispatchers.IO) {
        try {
            Git.open(repoPath)
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun getStatus(repoPath: File): Result<GitStatus> = withContext(Dispatchers.IO) {
        try {
            Git.open(repoPath).use { git ->
                val status = git.status().call()
                val result = GitStatus(
                    modified = status.modified.toList(),
                    added = status.added.toList(),
                    deleted = status.removed.toList(),
                    untracked = status.untracked.toList(),
                    conflicting = status.conflicting.toList()
                )
                Result.success(result)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun stageFiles(repoPath: File, filePatterns: List<String>): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            Git.open(repoPath).use { git ->
                filePatterns.forEach { pattern ->
                    git.add().addFilepattern(pattern).call()
                }
                Result.success(Unit)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun unstageFiles(repoPath: File, filePatterns: List<String>): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            Git.open(repoPath).use { git ->
                filePatterns.forEach { pattern ->
                    git.reset().addPath(pattern).call()
                }
                Result.success(Unit)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun commit(repoPath: File, message: String): Result<CommitInfo> = withContext(Dispatchers.IO) {
        try {
            Git.open(repoPath).use { git ->
                val revCommit = git.commit().setMessage(message).call()
                val info = CommitInfo(
                    id = revCommit.name,
                    shortMessage = revCommit.shortMessage,
                    fullMessage = revCommit.fullMessage,
                    authorName = revCommit.authorIdent.name,
                    authorEmail = revCommit.authorIdent.emailAddress,
                    date = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(revCommit.authorIdent.when),
                    timestamp = revCommit.authorIdent.when.time
                )
                Result.success(info)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun push(repoPath: File, username: String? = null, password: String? = null): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            Git.open(repoPath).use { git ->
                val pushCmd = git.push()
                if (!username.isNullOrEmpty() && !password.isNullOrEmpty()) {
                    pushCmd.setCredentialsProvider(UsernamePasswordCredentialsProvider(username, password))
                }
                pushCmd.call()
                Result.success(Unit)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun pull(repoPath: File, username: String? = null, password: String? = null): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            Git.open(repoPath).use { git ->
                val pullCmd = git.pull()
                if (!username.isNullOrEmpty() && !password.isNullOrEmpty()) {
                    pullCmd.setCredentialsProvider(UsernamePasswordCredentialsProvider(username, password))
                }
                pullCmd.call()
                Result.success(Unit)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun fetch(repoPath: File, username: String? = null, password: String? = null): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            Git.open(repoPath).use { git ->
                val fetchCmd = git.fetch()
                if (!username.isNullOrEmpty() && !password.isNullOrEmpty()) {
                    fetchCmd.setCredentialsProvider(UsernamePasswordCredentialsProvider(username, password))
                }
                fetchCmd.call()
                Result.success(Unit)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getBranches(repoPath: File): Result<List<String>> = withContext(Dispatchers.IO) {
        try {
            Git.open(repoPath).use { git ->
                val branches = git.branchList().call().map { ref ->
                    ref.name.substringAfter("refs/heads/")
                }
                Result.success(branches)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getCurrentBranch(repoPath: File): Result<String> = withContext(Dispatchers.IO) {
        try {
            Git.open(repoPath).use { git ->
                val branch = git.repository.fullBranch
                Result.success(branch.substringAfter("refs/heads/"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun switchBranch(repoPath: File, branchName: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            Git.open(repoPath).use { git ->
                git.checkout().setName(branchName).call()
                Result.success(Unit)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getCommitHistory(repoPath: File, maxCount: Int = 20): Result<List<CommitInfo>> = withContext(Dispatchers.IO) {
        try {
            Git.open(repoPath).use { git ->
                val commits = git.log().setMaxCount(maxCount).call().map { revCommit ->
                    CommitInfo(
                        id = revCommit.name,
                        shortMessage = revCommit.shortMessage,
                        fullMessage = revCommit.fullMessage,
                        authorName = revCommit.authorIdent.name,
                        authorEmail = revCommit.authorIdent.emailAddress,
                        date = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(revCommit.authorIdent.when),
                        timestamp = revCommit.authorIdent.when.time
                    )
                }
                Result.success(commits)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getRemoteOrigin(repoPath: File): Result<String> = withContext(Dispatchers.IO) {
        try {
            Git.open(repoPath).use { git ->
                val config = git.repository.config
                val origin = config.getString("remote", "origin", "url")
                Result.success(origin ?: "No remote configured")
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getLatestCommit(repoPath: File): Result<CommitInfo?> = withContext(Dispatchers.IO) {
        try {
            Git.open(repoPath).use { git ->
                val log = git.log().setMaxCount(1).call()
                val commit = log.firstOrNull()
                if (commit != null) {
                    val info = CommitInfo(
                        id = commit.name,
                        shortMessage = commit.shortMessage,
                        fullMessage = commit.fullMessage,
                        authorName = commit.authorIdent.name,
                        authorEmail = commit.authorIdent.emailAddress,
                        date = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(commit.authorIdent.when),
                        timestamp = commit.authorIdent.when.time
                    )
                    Result.success(info)
                } else {
                    Result.success(null)
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
