package com.example.macc2025.di

import android.content.Context
import com.example.macc2025.R
import com.example.macc2025.data.remote.PlacesRemoteDataSource
import com.example.macc2025.data.repository.PlacesRepositoryImpl
import com.example.macc2025.data.repository.UserRepositoryImpl
import com.example.macc2025.domain.repository.PlacesRepository
import com.example.macc2025.domain.repository.UserRepository
import com.example.macc2025.domain.usecase.GetPlaceDetailsUseCase
import com.example.macc2025.domain.usecase.SearchLocationsUseCase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.net.PlacesClient
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun providePlacesClient(@ApplicationContext context: Context): PlacesClient {
        Places.initialize(context, context.getString(R.string.google_maps_key))
        return Places.createClient(context)
    }

    @Provides
    @Singleton
    fun provideRemoteDataSource(client: PlacesClient): PlacesRemoteDataSource =
        PlacesRemoteDataSource(client)

    @Provides
    @Singleton
    fun provideRepository(remote: PlacesRemoteDataSource): PlacesRepository =
        PlacesRepositoryImpl(remote)

    @Provides
    @Singleton
    fun provideFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance()

    @Provides
    @Singleton
    fun provideUserRepository(db: FirebaseFirestore): UserRepository =
        UserRepositoryImpl(db)

    @Provides
    fun provideSearchLocationsUseCase(repo: PlacesRepository): SearchLocationsUseCase =
        SearchLocationsUseCase(repo)

    @Provides
    fun provideGetPlaceDetailsUseCase(repo: PlacesRepository): GetPlaceDetailsUseCase =
        GetPlaceDetailsUseCase(repo)
}
