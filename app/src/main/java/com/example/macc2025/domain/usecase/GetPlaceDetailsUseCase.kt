package com.example.macc2025.domain.usecase

import com.example.macc2025.domain.repository.PlacesRepository
import com.google.android.gms.maps.model.LatLng
import javax.inject.Inject

class GetPlaceDetailsUseCase @Inject constructor(
    private val repository: PlacesRepository
) {
    suspend operator fun invoke(placeId: String): LatLng {
        return repository.getPlaceLatLng(placeId)
    }
}
