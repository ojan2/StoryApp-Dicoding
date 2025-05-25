package com.application.storyapp.view

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.application.storyapp.AddStoryViewModel
import com.application.storyapp.ViewModelFactory
import com.application.storyapp.createCustomTempFile
import com.application.storyapp.databinding.FragmentAddStoryBinding
import com.application.storyapp.model.AddStoryUIState
import com.application.storyapp.reduceFileImage
import com.application.storyapp.uriToFile
import java.io.File
import android.Manifest




class AddStoryFragment : Fragment() {

    private var _binding: FragmentAddStoryBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: AddStoryViewModel
    private var currentPhotoPath: String? = null

    // Permission launcher
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            showImageSourceDialog()
        } else {
            showPermissionDeniedDialog()
        }
    }

    // Camera launcher
    private val launcherIntentCamera = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            currentPhotoPath?.let { path ->
                val file = File(path)
                val bitmap = BitmapFactory.decodeFile(file.path)
                binding.ivPreview.setImageBitmap(bitmap)

                val reducedFile = reduceFileImage(file)
                viewModel.setImageFile(reducedFile)
            }
        }
    }

    // Gallery launcher
    private val launcherIntentGallery = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val selectedImg = result.data?.data as Uri
            selectedImg.let { uri ->
                val myFile = uriToFile(uri, requireContext())
                binding.ivPreview.setImageURI(uri)

                val reducedFile = reduceFileImage(myFile)
                viewModel.setImageFile(reducedFile)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddStoryBinding.inflate(inflater, container, false)
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
        viewModel = ViewModelProvider(this, factory)[AddStoryViewModel::class.java]
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
            event.getContentIfNotHandled()?.let { message ->
                showSuccessDialog(message)
            }
        }

        viewModel.navigateBackEvent.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let {
                safeNavigateBack()
            }
        }
    }

    private fun updateUI(state: AddStoryUIState) {
        // Loading state
        binding.buttonAdd.isEnabled = !state.isLoading
        binding.progressBar.visibility = if (state.isLoading) View.VISIBLE else View.GONE

        // Error states
        binding.tilDescription.error = state.descriptionError

        // Image error
        if (state.imageError != null) {
            binding.tvImageError.text = state.imageError
            binding.tvImageError.visibility = View.VISIBLE
        } else {
            binding.tvImageError.visibility = View.GONE
        }

        // Disable inputs while loading
        binding.edAddDescription.isEnabled = !state.isLoading
        binding.btnCamera.isEnabled = !state.isLoading
        binding.btnGallery.isEnabled = !state.isLoading
    }

    private fun setupListeners() {
        // Description validation
        binding.edAddDescription.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                viewModel.validateDescription(s?.toString() ?: "")
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        // Camera button
        binding.btnCamera.setOnClickListener {
            checkCameraPermission()
        }

        // Gallery button
        binding.btnGallery.setOnClickListener {
            openGallery()
        }

        // Upload button
        binding.buttonAdd.setOnClickListener {
            val description = binding.edAddDescription.text?.toString() ?: ""
            viewModel.uploadStory(description)
        }
    }

    private fun checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            openCamera()
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun showImageSourceDialog() {
        val options = arrayOf("Camera", "Gallery")
        AlertDialog.Builder(requireContext())
            .setTitle("Select Image Source")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> openCamera()
                    1 -> openGallery()
                }
            }
            .show()
    }

    private fun openCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.resolveActivity(requireActivity().packageManager)

        createCustomTempFile(requireActivity().application).also {
            val photoURI: Uri = FileProvider.getUriForFile(
                requireContext(),
                "${requireContext().packageName}.fileprovider",
                it
            )
            currentPhotoPath = it.absolutePath
            intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
            launcherIntentCamera.launch(intent)
        }
    }

    private fun openGallery() {
        val intent = Intent()
        intent.action = Intent.ACTION_GET_CONTENT
        intent.type = "image/*"
        val chooser = Intent.createChooser(intent, "Choose a Picture")
        launcherIntentGallery.launch(chooser)
    }

    private fun showPermissionDeniedDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Permission Required")
            .setMessage("Camera permission is required to take photos")
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun showErrorDialog(message: String) {
        AlertDialog.Builder(requireContext())
            .setTitle("Upload Failed")
            .setMessage(message)
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun showSuccessDialog(message: String) {
        AlertDialog.Builder(requireContext())
            .setTitle("Upload Successful")
            .setMessage(message)
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
                safeNavigateBack()
            }
            .setCancelable(false)
            .show()
    }

    /**
     * Safe navigation method that handles fragment lifecycle issues
     */
    private fun safeNavigateBack() {
        try {
            // Check if fragment is still attached and active
            if (isAdded && !isDetached && !isRemoving && view != null) {
                // Try to navigate using NavController
                findNavController().popBackStack()
            } else {
                // Fallback: finish activity or use alternative navigation
                activity?.onBackPressed()
            }
        } catch (e: IllegalStateException) {
            // Handle case where NavController is not available
            try {
                // Alternative: try to finish activity
                activity?.onBackPressed()
            } catch (ex: Exception) {
                // Last resort: just finish the activity
                activity?.finish()
            }
        } catch (e: Exception) {
            // Handle any other navigation exceptions
            activity?.onBackPressed()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}