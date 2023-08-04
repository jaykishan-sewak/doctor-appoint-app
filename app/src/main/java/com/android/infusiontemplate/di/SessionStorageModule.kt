package com.android.infusiontemplate.di

import com.android.infusiontemplate.repository.local.PreferenceDataStore
import com.android.infusiontemplate.repository.local.Session
import dagger.Binds
import dagger.Module

@Module
abstract class SessionStorageModule {

    // Makes Dagger provide SharedPreferencesStorage when a Storage type is requested
    @Binds
    abstract fun provideSession(storage: PreferenceDataStore): Session
}
