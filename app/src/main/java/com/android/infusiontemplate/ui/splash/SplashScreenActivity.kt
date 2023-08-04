package com.android.infusiontemplate.ui.splash

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import com.android.infusiontemplate.repository.local.PreferenceDataStore
import com.android.infusiontemplate.repository.local.USER_IS_LOGGED_IN
import com.android.infusiontemplate.ui.authentication.AuthenticationActivity
import com.android.infusiontemplate.ui.dashboard.DashboardActivity
import com.android.infusiontemplate.util.extension.startActivityFinish
import kotlinx.coroutines.launch

class SplashScreenActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen()
        lifecycleScope.launch {
            PreferenceDataStore(this@SplashScreenActivity.applicationContext).getBoolean(
                USER_IS_LOGGED_IN).asLiveData().observe(
                this@SplashScreenActivity, {
                    if (it == true) {
                        startActivityFinish<DashboardActivity> { }
                    } else {
                        startActivityFinish<AuthenticationActivity> { }
                    }
                }
            )
        }
    }
}