package com.android.doctorapp.ui.splash

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import com.android.doctorapp.repository.local.PreferenceDataStore
import com.android.doctorapp.repository.local.USER_IS_LOGGED_IN
import com.android.doctorapp.ui.authentication.AuthenticationActivity
import com.android.doctorapp.ui.doctordashboard.DoctorDashboardActivity
import com.android.doctorapp.util.extension.startActivityFinish
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
                        startActivityFinish<DoctorDashboardActivity> { }
                    } else {
                        startActivityFinish<AuthenticationActivity> { }
                    }
                }
            )
        }
    }
}