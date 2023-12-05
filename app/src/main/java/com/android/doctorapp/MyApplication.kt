package com.android.doctorapp

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import com.android.doctorapp.di.AppComponent
import com.android.doctorapp.di.AppComponentProvider
import com.android.doctorapp.di.DaggerAppComponent
import com.android.doctorapp.di.ResourceProvider
import com.android.doctorapp.di.core.CoreComponent
import com.android.doctorapp.di.core.CoreComponentProvider
import com.android.doctorapp.di.core.CoreModule
import com.android.doctorapp.di.core.DaggerCoreComponent
import com.android.doctorapp.repository.local.IS_ENABLED_DARK_MODE
import com.android.doctorapp.repository.local.PreferenceDataStore
import com.google.firebase.FirebaseApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MyApplication : Application(), AppComponentProvider, CoreComponentProvider, ResourceProvider {

    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this);

        val sharedPreference = PreferenceDataStore(applicationContext)
        CoroutineScope(Dispatchers.Main).launch {

            sharedPreference.getBoolean(IS_ENABLED_DARK_MODE).collectLatest {
                if (it == true)
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                else
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

            }
        }
    }

    override fun getAppComponent(): AppComponent {
        return DaggerAppComponent.builder()
            .coreComponent(getCoreComponent()).build()
    }


    override fun getCoreComponent(): CoreComponent {
        return DaggerCoreComponent.builder().coreModule(CoreModule(this)).build()
    }
}
