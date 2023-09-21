package com.android.doctorapp.ui.splash

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import com.android.doctorapp.repository.local.PreferenceDataStore
import com.android.doctorapp.repository.local.USER_TYPE
import com.android.doctorapp.ui.admin.AdminDashboardActivity
import com.android.doctorapp.ui.authentication.AuthenticationActivity
import com.android.doctorapp.ui.doctordashboard.DoctorDashboardActivity
import com.android.doctorapp.ui.userdashboard.UserDashboardActivity
import com.android.doctorapp.util.constants.ConstantKey.USER_TYPE_ADMIN
import com.android.doctorapp.util.constants.ConstantKey.USER_TYPE_DOCTOR
import com.android.doctorapp.util.constants.ConstantKey.USER_TYPE_USER
import com.android.doctorapp.util.extension.startActivityFinish
import kotlinx.coroutines.launch

class SplashScreenActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen()
        lifecycleScope.launch {
            PreferenceDataStore(this@SplashScreenActivity.applicationContext).getString(
                USER_TYPE
            ).asLiveData().observe(
                this@SplashScreenActivity
            ) {
                when (it) {
                    USER_TYPE_DOCTOR -> {
                        startActivityFinish<DoctorDashboardActivity> { }
                    }

                    USER_TYPE_USER -> {
                        startActivityFinish<UserDashboardActivity> { }
                    }

                    USER_TYPE_ADMIN -> {
                        startActivityFinish<AdminDashboardActivity> { }
                    }

                    else -> {
                        startActivityFinish<AuthenticationActivity> { }
                    }
                }
            }
        }
    }
}