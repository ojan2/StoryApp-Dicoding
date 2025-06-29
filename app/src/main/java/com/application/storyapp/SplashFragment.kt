package com.application.storyapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.application.storyapp.R
import com.application.storyapp.data.data_store.UserPreferences
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SplashFragment : Fragment() {

    private lateinit var userPreferences: UserPreferences

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_splash, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        userPreferences = UserPreferences.getInstance(requireContext())

        lifecycleScope.launch {
            delay(1500) // kasih delay animasi splash kalau ada
            userPreferences.isLoggedIn().collect { isLoggedIn ->
                val destination = if (isLoggedIn) R.id.homeFragment else R.id.welcomeFragment
                findNavController().navigate(
                    destination,
                    null,
                    NavOptions.Builder()
                        .setPopUpTo(R.id.splashFragment, true) // supaya Splash nggak bisa diback
                        .build()
                )
            }
        }
    }
}
