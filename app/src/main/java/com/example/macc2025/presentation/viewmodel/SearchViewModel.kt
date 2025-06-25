package com.example.macc2025.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.macc2025.domain.usecase.GetPlaceDetailsUseCase
import com.example.macc2025.domain.usecase.SearchLocationsUseCase
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.model.AutocompletePrediction
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val searchLocations: SearchLocationsUseCase,
    private val getPlaceDetails: GetPlaceDetailsUseCase
) : ViewModel() {

    private val _predictions = MutableStateFlow<List<AutocompletePrediction>>(emptyList())
    val predictions: StateFlow<List<AutocompletePrediction>> = _predictions

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private val _selectedLocation = MutableStateFlow<LatLng?>(null)
    val selectedLocation: StateFlow<LatLng?> = _selectedLocation

    fun search(query: String) {
        if (query.isBlank()) {
            _predictions.value = emptyList()
            return
        }
        viewModelScope.launch {
            try {
                _predictions.value = searchLocations(query)
                _errorMessage.value = null
            } catch (e: Exception) {
                _predictions.value = emptyList()
                _errorMessage.value = e.localizedMessage
            }
        }
    }

    fun fetchPlace(placeId: String, onComplete: (LatLng) -> Unit) {
        viewModelScope.launch {
            try {
                val ll = getPlaceDetails(placeId)
                _selectedLocation.value = ll
                onComplete(ll)
            } catch (e: Exception) {
                _errorMessage.value = e.localizedMessage
            }
        }
    }
}
