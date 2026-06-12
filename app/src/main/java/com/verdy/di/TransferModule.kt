package com.verdy.di

import com.verdy.data.transfer.GardenFileManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object TransferModule
// GardenFileManager uses @Singleton + @Inject constructor — Hilt auto-provides it.
// This module exists as an extension point for future transfer-related bindings.
