package com.android.doctorapp.util.extension

import android.app.DatePickerDialog
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.android.doctorapp.BuildConfig
import java.util.Calendar

fun Context.toast(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}

fun Context.toast(message: String, callback: (isToastShown: Boolean) -> Unit) {
    val toast = Toast.makeText(this, message, Toast.LENGTH_SHORT)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        toast.addCallback(object : Toast.Callback() {
            override fun onToastHidden() {
                super.onToastHidden()
                callback.invoke(false)
            }

            override fun onToastShown() {
                super.onToastShown()
                callback.invoke(true)
            }
        })
    }
    toast.show()
}

fun Context.longToast(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_LONG).show()
}

fun Fragment.e(message: String) {
    if (BuildConfig.DEBUG) {
        Log.e(this::class.java.simpleName, message)
    }
}

fun Context.e(message: String) {
    if (BuildConfig.DEBUG) {
        Log.e(this::class.java.simpleName, message)
    }
}

fun Fragment.d(message: String) {
    if (BuildConfig.DEBUG) {
        Log.e(this::class.java.simpleName, message)
    }
}

fun Context.d(message: String) {
    if (BuildConfig.DEBUG) {
        Log.e(this::class.java.simpleName, message)
    }
}

fun Context.isNetworkAvailable(): Boolean {
    val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val nw = connectivityManager.activeNetwork ?: return false
    val actNw = connectivityManager.getNetworkCapabilities(nw) ?: return false
    return when {
        actNw.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
        actNw.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
        //for other device how are able to connect with Ethernet
        actNw.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
        //for check internet over Bluetooth
        actNw.hasTransport(NetworkCapabilities.TRANSPORT_BLUETOOTH) -> true
        else -> false
    }

}

fun Context.selectDate(maxDate: Long?, minDate: Long?, handleClick: (date: String) -> Unit) {
    val myCalendar = Calendar.getInstance()
    val date = DatePickerDialog.OnDateSetListener { view, year, month, dayOfMonth ->
        val selectedDate = String.format("%02d-%02d-%04d", dayOfMonth, month + 1, year)
        myCalendar.set(Calendar.YEAR, year)
        myCalendar.set(Calendar.MONTH, month)
        myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
        handleClick.invoke(selectedDate)
    }

    val datePickerDialog = DatePickerDialog(
        this, date, myCalendar[Calendar.YEAR], myCalendar[Calendar.MONTH],
        myCalendar[Calendar.DAY_OF_MONTH]
    )
    maxDate?.let { datePickerDialog.datePicker.maxDate = it }
    minDate?.let { datePickerDialog.datePicker.minDate = it }
    datePickerDialog.show()
}
