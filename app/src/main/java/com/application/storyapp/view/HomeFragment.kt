package com.application.storyapp.view

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.application.storyapp.LoginViewModel
import com.application.storyapp.R
import com.application.storyapp.databinding.FragmentHomeBinding
import com.application.storyapp.databinding.FragmentLoginBinding


class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        binding.btnAddStory.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_addStoryFragment)
        }
        return binding.root
    }
}