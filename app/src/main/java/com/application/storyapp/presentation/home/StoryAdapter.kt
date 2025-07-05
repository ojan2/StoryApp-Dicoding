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

class StoryAdapter(
    private val onItemClick: (Story, View, View, View) -> Unit
) : PagingDataAdapter<Story, StoryAdapter.StoryViewHolder>(DIFF_CALLBACK) {

    inner class StoryViewHolder(private val binding: ItemStoryBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(story: Story) {
            binding.apply {
                tvName.text = story.name
                tvDescription.text = story.description

                Glide.with(itemView.context)
                    .load(story.photoUrl)
                    .override(800, 800)
                    .into(ivStoryImage)

                ivStoryImage.transitionName = "photo_big_${story.id}"
                tvName.transitionName = "name_${story.id}"
                tvDescription.transitionName = "description_${story.id}"

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

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Story>() {
            override fun areItemsTheSame(oldItem: Story, newItem: Story) = oldItem.id == newItem.id
            override fun areContentsTheSame(oldItem: Story, newItem: Story) = oldItem == newItem
        }
    }
}
