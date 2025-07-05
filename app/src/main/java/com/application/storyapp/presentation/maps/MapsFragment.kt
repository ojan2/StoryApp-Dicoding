package com.application.storyapp.presentation.maps

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.application.storyapp.R
import com.application.storyapp.data.ViewModelFactory
import com.application.storyapp.databinding.FragmentMapsBinding
import com.application.storyapp.model.Story
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions


class MapsFragment : Fragment() {

    private var _binding: FragmentMapsBinding? = null
    private val binding get() = _binding!!

    private lateinit var mMap: GoogleMap
    private lateinit var mapsViewModel: MapsViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View{
        _binding = FragmentMapsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        setupViewModel()


        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync { googleMap ->
            mMap = googleMap
            setupMap()
            mapsViewModel.onMapReady()
        }

        setupObservers()
    }

    private fun setupViewModel() {
        val factory = ViewModelFactory(requireContext())
        mapsViewModel = ViewModelProvider(this, factory)[MapsViewModel::class.java]
    }

    private fun setupObservers() {
        mapsViewModel.storiesWithLocation.observe(viewLifecycleOwner) { stories ->
            if (::mMap.isInitialized && stories.isNotEmpty()) {
                addMarkersToMap(stories)
                showAllMarkers()
            }
        }

        mapsViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        mapsViewModel.errorMessage.observe(viewLifecycleOwner) { errorMessage ->
            errorMessage?.let {
                showErrorMessage(it)
                mapsViewModel.clearError()
            }
        }

        mapsViewModel.isMapReady.observe(viewLifecycleOwner) { isReady ->
            if (isReady) {
                mapsViewModel.loadStoriesWithLocation()
            }
        }
    }

    private fun setupMap() {
        with(mMap.uiSettings) {
            isZoomControlsEnabled = true
            isIndoorLevelPickerEnabled = true
            isCompassEnabled = true
            isMapToolbarEnabled = true
        }

        mMap.mapType = GoogleMap.MAP_TYPE_NORMAL

        // Set marker
        mMap.setOnMarkerClickListener { marker ->
            marker.showInfoWindow()
            true
        }

        mMap.setOnInfoWindowClickListener { marker ->
            Toast.makeText(
                requireContext(),
                "Story: ${marker.title}",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun addMarkersToMap(stories: List<Story>) {
        mMap.clear()

        stories.forEach { story ->
            story.lat?.let { lat ->
                story.lon?.let { lon ->
                    val latLng = LatLng(lat.toDouble(), lon.toDouble())

                    mMap.addMarker(
                        MarkerOptions()
                            .position(latLng)
                            .title(story.name)
                            .snippet(story.description)
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
                    )?.tag = story
                }
            }
        }
    }

    private fun showAllMarkers() {
        val bounds = mapsViewModel.getLatLngBounds()
        bounds?.let {
            try {
                val padding = 100
                val cameraUpdate = CameraUpdateFactory.newLatLngBounds(it, padding)
                mMap.animateCamera(cameraUpdate)
            } catch (e: Exception) {
                Log.e("MapsFragment", "Error adjusting camera bounds: ${e.message}")
                showDefaultLocation()
            }
        } ?: showDefaultLocation()
    }

    private fun showDefaultLocation() {
        val jakarta = LatLng(-6.2088, 106.8456)
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(jakarta, 10f))
    }

    private fun showErrorMessage(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        Log.e("MapsFragment", "Error: $message")

        if (::mMap.isInitialized) {
            showDefaultLocation()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}