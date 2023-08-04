package com.android.infusiontemplate.di.core

import android.app.Application
import android.content.Context
import com.android.infusiontemplate.di.ResourceProvider
import com.android.infusiontemplate.di.SessionStorageModule
import com.android.infusiontemplate.repository.local.Session
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
