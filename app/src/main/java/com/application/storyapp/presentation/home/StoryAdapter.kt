package com.application.storyapp.presentation.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.application.storyapp.databinding.ItemStoryBinding
import com.application.storyapp.model.Story
import com.bumptech.glide.Glide

class StoryAdapter(
    private val list: List<Story>,
    private val onItemClick: (Story, View, View, View) -> Unit
) : RecyclerView.Adapter<StoryAdapter.StoryViewHolder>() {

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

                // Set unique transition name per item (penting!)
                ivStoryImage.transitionName = "photo_big_${story.id}"
                tvName.transitionName = "name_${story.id}"
                tvDescription.transitionName = "description_${story.id}"

                // Set click listener with view references
                root.setOnClickListener {
                    onItemClick(story, ivStoryImage, tvName, tvDescription)
                }
            }
    }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StoryViewHolder {
        val binding = ItemStoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return StoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: StoryViewHolder, position: Int) {
        holder.bind(list[position])
    }

    override fun getItemCount(): Int = list.size
}