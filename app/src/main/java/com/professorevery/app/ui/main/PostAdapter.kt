package com.professorevery.app.ui.main

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.professorevery.app.R
import com.professorevery.app.databinding.ItemPostBinding
import com.professorevery.app.model.Post
import java.text.SimpleDateFormat
import java.util.*

class PostAdapter(
    private val onLikeClick: (Post) -> Unit,
    private val onCommentClick: (Post) -> Unit
) : ListAdapter<Post, PostAdapter.PostViewHolder>(PostDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val binding = ItemPostBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return PostViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class PostViewHolder(
        private val binding: ItemPostBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(post: Post) {
            binding.apply {
                authorName.text = post.authorName
                postTitle.text = post.title
                postContent.text = post.content
                postTime.text = getTimeAgo(post.timestamp)
                likeCount.text = post.likes.toString()
                commentCount.text = post.comments.toString()

                val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
                val isLiked = currentUserId != null && post.likedBy.contains(currentUserId)
                likeIcon.setImageResource(
                    if (isLiked) R.drawable.ic_favorite
                    else R.drawable.ic_favorite_border
                )

                likeButton.setOnClickListener { onLikeClick(post) }
                commentButton.setOnClickListener { onCommentClick(post) }
                root.setOnClickListener { onCommentClick(post) }
            }
        }

        private fun getTimeAgo(timestamp: Long): String {
            val now = System.currentTimeMillis()
            val diff = now - timestamp

            return when {
                diff < 60_000 -> "방금 전"
                diff < 3600_000 -> "${diff / 60_000}분 전"
                diff < 86400_000 -> "${diff / 3600_000}시간 전"
                else -> SimpleDateFormat("yyyy.MM.dd", Locale.getDefault())
                    .format(Date(timestamp))
            }
        }
    }

    private class PostDiffCallback : DiffUtil.ItemCallback<Post>() {
        override fun areItemsTheSame(oldItem: Post, newItem: Post): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Post, newItem: Post): Boolean {
            return oldItem == newItem
        }
    }
} 