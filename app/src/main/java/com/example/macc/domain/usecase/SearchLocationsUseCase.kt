package com.example.macc.domain.usecase

import com.example.macc.domain.repository.PlacesRepository
import com.google.android.libraries.places.api.model.AutocompletePrediction
import javax.inject.Inject

class SearchLocationsUseCase @Inject constructor(
    private val repository: PlacesRepository
) {
    suspend operator fun invoke(query: String): List<AutocompletePrediction> {
        return repository.searchLocations(query)
    }
}
