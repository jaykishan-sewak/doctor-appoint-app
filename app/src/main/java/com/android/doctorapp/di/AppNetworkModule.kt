package com.android.doctorapp.di

import com.android.doctorapp.repository.network.AppApi
import dagger.Module
import dagger.Provides
import dagger.Reusable
import retrofit2.Retrofit

@Module
class AppNetworkModule {
    @Reusable
    @Provides
    fun provideAuthApi(retrofit: Retrofit): AppApi {
        return retrofit.create(AppApi::class.java)
    }


}
