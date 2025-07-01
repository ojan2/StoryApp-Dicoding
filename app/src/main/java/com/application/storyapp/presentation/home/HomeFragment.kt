package com.application.storyapp.presentation.home

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.core.content.edit
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.navigation.navOptions
import androidx.recyclerview.widget.LinearLayoutManager
import com.application.storyapp.presentation.widget.ImagesBannerWidget
import com.application.storyapp.R
import com.application.storyapp.data.ViewModelFactory
import com.application.storyapp.data.data_store.UserPreferences
import com.application.storyapp.databinding.FragmentHomeBinding
import com.application.storyapp.model.Story
import com.google.gson.Gson
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: HomeViewModel
    private lateinit var userPreferences: UserPreferences

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupViewModel()
        setupRecyclerView()
        observeViewModel()
        handleBackPressed()

        parentFragmentManager.setFragmentResultListener("upload_result", viewLifecycleOwner) { _, result ->
            val success = result.getBoolean("success", false)
            if (success) {
                viewModel.loadStories()
            }
        }
        userPreferences = UserPreferences.getInstance(requireContext())

        binding.btnLogout.setOnClickListener {
            logout()
        }

        binding.btnAddStory.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_addStoryFragment)
        }

        binding.swipeRefresh.setOnRefreshListener {
            viewModel.loadStories()
        }
    }

    private fun setupViewModel() {
        val factory = ViewModelFactory(requireContext())
        viewModel = ViewModelProvider(this, factory)[HomeViewModel::class.java]
    }

    private fun setupRecyclerView() {
        binding.rvStory.layoutManager = LinearLayoutManager(requireContext())
    }

    private fun observeViewModel() {
        viewModel.stories.observe(viewLifecycleOwner) { list ->
            if (!list.isNullOrEmpty()) {
                val json = Gson().toJson(list)
                requireContext().getSharedPreferences("widget_prefs", Context.MODE_PRIVATE).edit {
                    putString("stories", json)
                }
                ImagesBannerWidget.updateWidget(requireContext())
            }

            binding.rvStory.adapter = StoryAdapter(list) { story, imgView, nameView, descView ->
                navigateToDetailWithTransition(story, imgView, nameView, descView)
            }
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { message ->
            message?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.swipeRefresh.isRefreshing = isLoading
        }
    }

    private fun navigateToDetailWithTransition(
        story: Story,
        imageView: View,
        nameView: View,
        descView: View
    ) {
        val action = HomeFragmentDirections.actionHomeFragmentToDetailFragment(story)

        val extras = FragmentNavigatorExtras(
            imageView to "photo_big_${story.id}",
            nameView to "name_${story.id}",
            descView to "description_${story.id}"
        )

        findNavController().navigate(action, extras)
    }
    private fun logout() {
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.confim_logout)
            .setMessage(R.string.message_exit)
            .setPositiveButton(R.string.yes) { _, _ ->
                lifecycleScope.launch {
                    userPreferences.clearAuthData()
                    findNavController().navigate(
                        R.id.loginFragment,
                        null,
                        navOptions {
                            popUpTo(R.id.navigation_menu) {
                                inclusive = true
                            }
                            launchSingleTop = true
                        }
                    )
                }
            }
            .setNegativeButton(R.string.cancel) { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun handleBackPressed() {
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    requireActivity().finishAffinity()
                }
            }
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
