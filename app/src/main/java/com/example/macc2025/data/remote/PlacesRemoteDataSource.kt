package com.example.macc2025.data.remote

import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.PlacesClient
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class PlacesRemoteDataSource(
    private val client: PlacesClient
) {
    suspend fun searchLocations(query: String): List<AutocompletePrediction> =
        suspendCancellableCoroutine { cont ->
            val request = FindAutocompletePredictionsRequest.builder()
                .setQuery(query)
                .build()
            client.findAutocompletePredictions(request)
                .addOnSuccessListener { resp ->
                    cont.resume(resp.autocompletePredictions)
                }
                .addOnFailureListener { err ->
                    cont.resumeWithException(err)
                }
        }

    suspend fun getPlaceLatLng(placeId: String): LatLng =
        suspendCancellableCoroutine { cont ->
            val request = FetchPlaceRequest.builder(
                placeId,
                listOf(Place.Field.LAT_LNG)
            ).build()
            client.fetchPlace(request)
                .addOnSuccessListener { resp ->
                    val ll = resp.place.latLng
                    if (ll != null) {
                        cont.resume(ll)
                    } else {
                        cont.resumeWithException(IllegalStateException("No LatLng"))
                    }
                }
                .addOnFailureListener { err ->
                    cont.resumeWithException(err)
                }
        }
}

