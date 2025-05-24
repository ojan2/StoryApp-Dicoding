package com.application.storyapp.view

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
import com.application.storyapp.LoginViewModel
import com.application.storyapp.R
import com.application.storyapp.ViewModelFactory
import com.application.storyapp.databinding.FragmentLoginBinding
import com.application.storyapp.model.LoginResult
import com.application.storyapp.model.LoginUIState
import com.application.storyapp.network.ApiClient
import com.application.storyapp.network.AuthRepository


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
    }

    private fun setupViewModel() {
        val factory = ViewModelFactory(requireContext())
        viewModel = ViewModelProvider(this, factory)[LoginViewModel::class.java]
    }

    private fun setupObservers() {
        viewModel.uiState.observe(viewLifecycleOwner) { state ->
            updateUI(state)
        }

        // Event-based approach for dialogs
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
        // Loading state
        binding.btnLogin.isEnabled = !state.isLoading
        binding.progressBar.visibility = if (state.isLoading) View.VISIBLE else View.GONE

        // Error states for input fields
        binding.textInputLayout.error = state.emailError
        binding.textInputLayout2.error = state.passwordError

        // Disable input fields while loading
        binding.edEmail.isEnabled = !state.isLoading
        binding.edPassword.isEnabled = !state.isLoading
    }

    private fun setupListeners() {
        // Email validation
        binding.edEmail.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                viewModel.validateEmail(s?.toString() ?: "")
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        // Password validation
        binding.edPassword.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                viewModel.validatePassword(s?.toString() ?: "")
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        // Login button click
        binding.btnLogin.setOnClickListener {
            val email = binding.edEmail.text?.toString() ?: ""
            val password = binding.edPassword.text?.toString() ?: ""
            viewModel.login(email, password)
        }

        // Navigate to register (if you have this button)
//        binding.tvRegister?.setOnClickListener {
//            findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
//        }
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

    private fun showSuccessDialog(loginResult: LoginResult) {
        AlertDialog.Builder(requireContext())
            .setTitle("Login Successful")
            .setMessage("Welcome back, ${loginResult.name}!")
            .setPositiveButton("Continue") { dialog, _ ->
                dialog.dismiss()
              //  navigateToMainActivity()
            }
            .setCancelable(false)
            .show()
    }

//    private fun navigateToMainActivity() {
//        // Navigate to MainActivity or Main Screen
//        val intent = Intent(requireContext(), MainActivity::class.java)
//        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
//        startActivity(intent)
//        requireActivity().finish()
//    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}