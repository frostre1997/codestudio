package com.android.codestudio.app.data

data class Repository(
    val name: String,
    val context: String,
    val branch: String = "main",
    val status: String = "No Changes",
    val lastActive: String = "a few seconds ago"
)

data class Extension(
    val name: String,
    val description: String,
    val installed: Boolean = false
)
