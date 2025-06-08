package com.professorevery.app.model

data class Comment(
    val id: String = "",
    val postId: String = "",
    val authorId: String = "",
    val authorName: String = "",
    val content: String = "",
    val timestamp: Long = 0
) {
    fun toMap(): Map<String, Any> {
        return mapOf(
            "id" to id,
            "postId" to postId,
            "authorId" to authorId,
            "authorName" to authorName,
            "content" to content,
            "timestamp" to timestamp
        )
    }
} 