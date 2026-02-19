package com.example.yourroom.di

import android.content.Context
import com.example.yourroom.datastore.FavoriteSpacesStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object LocalStorageModule {

    @Provides
    @Singleton
    fun provideFavoriteSpacesStore(
        @ApplicationContext context: Context
    ): FavoriteSpacesStore = FavoriteSpacesStore(context)
}

