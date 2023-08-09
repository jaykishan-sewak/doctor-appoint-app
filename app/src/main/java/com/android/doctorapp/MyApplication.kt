package com.android.doctorapp

import android.app.Application
import com.android.doctorapp.di.AppComponent
import com.android.doctorapp.di.AppComponentProvider
import com.android.doctorapp.di.core.CoreComponent
import com.android.doctorapp.di.DaggerAppComponent
import com.android.doctorapp.di.ResourceProvider
import com.android.doctorapp.di.core.CoreComponentProvider
import com.android.doctorapp.di.core.CoreModule
import com.android.doctorapp.di.core.DaggerCoreComponent
import com.google.firebase.FirebaseApp

class MyApplication : Application(), AppComponentProvider , CoreComponentProvider, ResourceProvider {

    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this);
    }

    override fun getAppComponent(): AppComponent {
        return DaggerAppComponent.builder()
            .coreComponent(getCoreComponent()).build()
    }


    override fun getCoreComponent(): CoreComponent {
        return DaggerCoreComponent.builder().coreModule(CoreModule(this)).build()
    }
}
