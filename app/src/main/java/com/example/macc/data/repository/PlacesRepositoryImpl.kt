package com.example.macc.data.repository

import com.example.macc.data.remote.PlacesRemoteDataSource
import com.example.macc.domain.repository.PlacesRepository
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.model.AutocompletePrediction

class PlacesRepositoryImpl(
    private val remote: PlacesRemoteDataSource
) : PlacesRepository {
    override suspend fun searchLocations(query: String): List<AutocompletePrediction> {
        return remote.searchLocations(query)
    }

    override suspend fun getPlaceLatLng(placeId: String): LatLng {
        return remote.getPlaceLatLng(placeId)
    }
}
