package com.her.app.data

data class Message(
    val id: String,
    val content: String,
    val isFromUser: Boolean,
    val timestampMs: Long = System.currentTimeMillis()
)
