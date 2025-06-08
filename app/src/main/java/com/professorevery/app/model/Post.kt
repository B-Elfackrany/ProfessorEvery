package com.professorevery.app.model

data class Post(
    val id: String = "",
    val authorId: String = "",
    val authorName: String = "",
    val title: String = "",
    val content: String = "",
    val timestamp: Long = 0,
    val likes: Int = 0,
    val comments: Int = 0,
    val likedBy: List<String> = listOf()
) {
    fun toMap(): Map<String, Any> {
        return mapOf(
            "id" to id,
            "authorId" to authorId,
            "authorName" to authorName,
            "title" to title,
            "content" to content,
            "timestamp" to timestamp,
            "likes" to likes,
            "comments" to comments,
            "likedBy" to likedBy
        )
    }
} 