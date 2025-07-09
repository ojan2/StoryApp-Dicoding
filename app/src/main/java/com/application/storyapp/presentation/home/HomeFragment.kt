package com.application.storyapp.presentation.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.navigation.navOptions
import androidx.paging.LoadState
import androidx.recyclerview.widget.LinearLayoutManager
import com.application.storyapp.R
import com.application.storyapp.data.ViewModelFactory
import com.application.storyapp.data.data_store.UserPreferences
import com.application.storyapp.databinding.FragmentHomeBinding
import com.application.storyapp.model.Story
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@Suppress("DEPRECATION")
class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: HomeViewModel
    private lateinit var userPreferences: UserPreferences
    private lateinit var storyAdapter: StoryAdapter

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
        setupListeners()
        observeViewModel()
        handleBackPressed()

        parentFragmentManager.setFragmentResultListener("upload_result",viewLifecycleOwner) { _, _ ->
            viewModel.refresh()
        }

        userPreferences = UserPreferences.getInstance(requireContext())
    }

    private fun setupViewModel() {
        val factory = ViewModelFactory(requireContext())
        viewModel = ViewModelProvider(this, factory)[HomeViewModel::class.java]
    }

    private fun setupRecyclerView() {
        storyAdapter = StoryAdapter { story, imgView, nameView, descView ->
            navigateToDetailWithTransition(story, imgView, nameView, descView)
        }

        binding.rvStory.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = storyAdapter.withLoadStateFooter(
                footer = LoadingStateAdapter { storyAdapter.retry() }
            )
        }
    }

    private fun setupListeners() {
        binding.apply {
            btnLogout.setOnClickListener { logout() }
            btnAddStory.setOnClickListener {
                findNavController().navigate(R.id.action_homeFragment_to_addStoryFragment)
            }
            btnMaps.setOnClickListener {
                findNavController().navigate(R.id.action_homeFragment_to_mapsFragment)
            }
            swipeRefresh.setOnRefreshListener {
                viewModel.refresh()
            }
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launchWhenStarted {
            viewModel.stories.collectLatest { pagingData ->
                storyAdapter.submitData(pagingData)
            }
        }

        lifecycleScope.launchWhenStarted {
            viewModel.isRefreshing.collect { isRefreshing ->
                binding.swipeRefresh.isRefreshing = isRefreshing
                if (!isRefreshing) hideShimmer()
            }
        }

        storyAdapter.addLoadStateListener { loadState ->
            when (loadState.refresh) {
                is LoadState.Loading -> {
                    if (!binding.swipeRefresh.isRefreshing) {
                        showShimmer()
                    }
                }

                is LoadState.Error -> {
                    binding.swipeRefresh.isRefreshing = false
                    hideShimmer()
                    showError((loadState.refresh as LoadState.Error).error.message)
                }

                else -> {
                    hideShimmer()
                }
            }
        }
    }

    private fun showShimmer() {
        binding.shimmerLayout.visibility = View.VISIBLE
        binding.shimmerLayout.startShimmer()
        binding.rvStory.visibility = View.GONE
    }

    private fun hideShimmer() {
        binding.shimmerLayout.stopShimmer()
        binding.shimmerLayout.visibility = View.GONE
        binding.rvStory.visibility = View.VISIBLE
    }
    private fun showError(message: String?) {
        val errorMsg = message ?: "Unknown error occurred"
        if (errorMsg.contains("Unable to resolve host")) {
            Snackbar.make(binding.root, "No internet connection", Snackbar.LENGTH_LONG)
                .setAction("Retry") { viewModel.refresh() }
                .show()
        } else {
            Toast.makeText(requireContext(), errorMsg, Toast.LENGTH_SHORT).show()
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
                            // Membersihkan seluruh back stack
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