package com.example.macc.domain.repository

import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.model.AutocompletePrediction

interface PlacesRepository {
    suspend fun searchLocations(query: String): List<AutocompletePrediction>
    suspend fun getPlaceLatLng(placeId: String): LatLng
}
