package com.application.storyapp.presentation.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.application.storyapp.databinding.ItemStoryBinding
import com.application.storyapp.model.Story
import com.bumptech.glide.Glide
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class StoryAdapter(
    private val onItemClick: (Story, View, View, View) -> Unit
) : PagingDataAdapter<Story, StoryAdapter.StoryViewHolder>(DIFF_CALLBACK) {

    inner class StoryViewHolder(private val binding: ItemStoryBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(story: Story) {
            binding.apply {
                tvName.text = story.name
                tvDescription.text = story.description
                tvPostTime.text = getTimeAgo(story.createdAt)

                Glide.with(itemView.context)
                    .load(story.photoUrl)
                    .override(800, 800)
                    .into(ivStoryImage)

                ivStoryImage.transitionName = "photo_big_${story.id}"
                tvName.transitionName = "name_${story.id}"
                tvDescription.transitionName = "description_${story.id}"
                tvPostTime.transitionName = "time_${story.id}"

                root.setOnClickListener {
                    onItemClick(story, ivStoryImage, tvName, tvDescription)
                }
            }
        }
    }

    override fun onBindViewHolder(holder: StoryViewHolder, position: Int) {
        val story = getItem(position)
        story?.let { holder.bind(it) }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StoryViewHolder {
        val binding = ItemStoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return StoryViewHolder(binding)
    }

    private fun getTimeAgo(createdAt: String): String {
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }
            val time = sdf.parse(createdAt)?.time ?: return "Just now"
            val now = System.currentTimeMillis()
            val diff = now - time

            when {
                diff < TimeUnit.MINUTES.toMillis(1) -> "Just now"
                diff < TimeUnit.HOURS.toMillis(1) -> {
                    val minutes = TimeUnit.MILLISECONDS.toMinutes(diff)
                    "$minutes minute${if (minutes != 1L) "s" else ""} ago"
                }
                diff < TimeUnit.DAYS.toMillis(1) -> {
                    val hours = TimeUnit.MILLISECONDS.toHours(diff)
                    "$hours hour${if (hours != 1L) "s" else ""} ago"
                }
                diff < TimeUnit.DAYS.toMillis(7) -> {
                    val days = TimeUnit.MILLISECONDS.toDays(diff)
                    "$days day${if (days != 1L) "s" else ""} ago"
                }
                diff < TimeUnit.DAYS.toMillis(30) -> {
                    val weeks = TimeUnit.MILLISECONDS.toDays(diff) / 7
                    "$weeks week${if (weeks != 1L) "s" else ""} ago"
                }
                diff < TimeUnit.DAYS.toMillis(365) -> {
                    val months = TimeUnit.MILLISECONDS.toDays(diff) / 30
                    "$months month${if (months != 1L) "s" else ""} ago"
                }
                else -> {
                    val years = TimeUnit.MILLISECONDS.toDays(diff) / 365
                    "$years year${if (years != 1L) "s" else ""} ago"
                }
            }
        } catch (e: Exception) {
            "Just now"
        }
    }
    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Story>() {
            override fun areItemsTheSame(oldItem: Story, newItem: Story) = oldItem.id == newItem.id
            override fun areContentsTheSame(oldItem: Story, newItem: Story) = oldItem == newItem
        }
    }
}