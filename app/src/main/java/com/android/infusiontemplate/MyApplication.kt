package com.android.infusiontemplate

import android.app.Application
import com.android.infusiontemplate.di.AppComponent
import com.android.infusiontemplate.di.AppComponentProvider
import com.android.infusiontemplate.di.core.CoreComponent
import com.android.infusiontemplate.di.DaggerAppComponent
import com.android.infusiontemplate.di.ResourceProvider
import com.android.infusiontemplate.di.core.CoreComponentProvider
import com.android.infusiontemplate.di.core.CoreModule
import com.android.infusiontemplate.di.core.DaggerCoreComponent

class MyApplication : Application(), AppComponentProvider , CoreComponentProvider, ResourceProvider {

    override fun onCreate() {
        super.onCreate()
    }

    override fun getAppComponent(): AppComponent {
        return DaggerAppComponent.builder()
            .coreComponent(getCoreComponent()).build()
    }


    override fun getCoreComponent(): CoreComponent {
        return DaggerCoreComponent.builder().coreModule(CoreModule(this)).build()
    }
}
