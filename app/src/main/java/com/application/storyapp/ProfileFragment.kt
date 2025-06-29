package com.application.storyapp

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.application.storyapp.databinding.FragmentProfileBinding
import com.application.storyapp.utils.LanguageHelper

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupLanguageSelection()
        loadSavedLanguage()
    }

    private fun setupLanguageSelection() {
        binding.RGlang.setOnCheckedChangeListener { group, checkedId ->
            when (checkedId) {
                R.id.langIndonesia -> changeLanguage("id")
                R.id.langEnglish -> changeLanguage("en")
            }
        }
    }

    private fun loadSavedLanguage() {
        val savedLanguage = LanguageHelper.getSavedLanguage(requireContext())
        when (savedLanguage) {
            "id" -> binding.langIndonesia.isChecked = true
            "en" -> binding.langEnglish.isChecked = true
        }
    }

    private fun changeLanguage(languageCode: String) {
        // Simpan bahasa terpilih
        LanguageHelper.setAppLanguage(requireContext(), languageCode)

        // Restart parent activity untuk menerapkan perubahan
        activity?.recreate()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}