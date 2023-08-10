package com.android.doctorapp.di.core

import android.app.Application
import android.content.Context
import com.android.doctorapp.di.ResourceProvider
import com.android.doctorapp.di.SessionStorageModule
import com.android.doctorapp.repository.local.Session
import dagger.Component
import retrofit2.Retrofit
import javax.inject.Singleton

@Singleton
@Component(modules = [CoreModule::class, SessionStorageModule::class])
interface CoreComponent {

    fun provideApplication(): Application

    fun provideApplicationContext(): Context

    fun provideRetrofit(): Retrofit

    fun provideResourceProvider(): ResourceProvider

    fun provideSession(): Session
}
