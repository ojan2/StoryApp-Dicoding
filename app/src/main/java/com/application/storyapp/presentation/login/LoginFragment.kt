package com.application.storyapp.presentation.login

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.navOptions
import com.application.storyapp.R
import com.application.storyapp.data.ViewModelFactory
import com.application.storyapp.databinding.FragmentLoginBinding
import com.application.storyapp.utils.LoginResult
import com.application.storyapp.utils.LoginUIState


class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: LoginViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViewModel()
        setupObservers()
        setupListeners()
        startImageAnimation()
    }

    private fun setupViewModel() {
        val factory = ViewModelFactory(requireContext())
        viewModel = ViewModelProvider(this, factory)[LoginViewModel::class.java]
    }

    private fun setupObservers() {
        viewModel.uiState.observe(viewLifecycleOwner) { state ->
            updateUI(state)
        }

        viewModel.errorEvent.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let { message ->
                showErrorDialog(message)
            }
        }

        viewModel.successEvent.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let { loginResult ->

                Log.d("LoginFragment", "Login successful in UI!")
                Log.d("LoginFragment", "User: ${loginResult.name}")
                Log.d("LoginFragment", "Token: ${loginResult.token}")
                showSuccessDialog(loginResult)
            }
        }

        viewModel.navigateToMainEvent.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let {
               // navigateToMainActivity()
            }
        }
    }

    private fun updateUI(state: LoginUIState) {

        binding.btnLogin.isEnabled = !state.isLoading
        binding.progressBar.visibility = if (state.isLoading) View.VISIBLE else View.GONE

        binding.textInputLayout.error = state.emailError
        binding.textInputLayout2.error = state.passwordError

        binding.edEmail.isEnabled = !state.isLoading
        binding.edPassword.isEnabled = !state.isLoading
    }

    private fun setupListeners() {

        binding.edEmail.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                viewModel.validateEmail(s?.toString() ?: "")
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        binding.edPassword.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                viewModel.validatePassword(s?.toString() ?: "")
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })


        binding.btnLogin.setOnClickListener {
            val email = binding.edEmail.text?.toString() ?: ""
            val password = binding.edPassword.text?.toString() ?: ""
            viewModel.login(email, password)
        }

    }

    private fun showErrorDialog(message: String) {
        AlertDialog.Builder(requireContext())
            .setTitle("Login Failed")
            .setMessage(message)
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    @SuppressLint("StringFormatInvalid")
    private fun showSuccessDialog(loginResult: LoginResult) {
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.login_succes)
            .setMessage(getString(R.string.welcome, loginResult.name))
            .setPositiveButton(R.string.continue_) { dialog, _ ->
                dialog.dismiss()
                findNavController().navigate(
                    R.id.action_loginFragment_to_homeFragment,
                    null,
                    navOptions {
                        popUpTo(R.id.splashFragment) {
                            inclusive = true
                        }
                    }
                )
            }
            .setCancelable(false)
            .show()
    }

    private fun startImageAnimation() {
        ObjectAnimator.ofFloat(binding.imageView, "translationX", -40f, 40f).apply {
            duration = 8000L
            repeatCount = ValueAnimator.INFINITE
            repeatMode = ValueAnimator.REVERSE
            start()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}