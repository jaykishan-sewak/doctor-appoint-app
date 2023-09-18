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
import com.android.doctorapp.util.constants.ConstantKey.DATE_AND_DAY_NAME_FORMAT
import com.android.doctorapp.util.constants.ConstantKey.DATE_MONTH_FORMAT
import com.android.doctorapp.util.constants.ConstantKey.FORMATTED_DATE
import com.android.doctorapp.util.constants.ConstantKey.FULL_DATE_FORMAT
import com.android.doctorapp.util.constants.ConstantKey.HOUR_MIN_AM_PM_FORMAT
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale


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

fun convertDate(originalDateStr: String): String {
    return try {
        val originalDateFormat = SimpleDateFormat(FULL_DATE_FORMAT, Locale.getDefault())
        val targetDateFormat = SimpleDateFormat(DATE_AND_DAY_NAME_FORMAT, Locale.getDefault())

        val calendar = Calendar.getInstance()
        calendar.time = originalDateFormat.parse(originalDateStr) ?: Date()

        targetDateFormat.format(calendar.time)
    } catch (e: Exception) {
        e.printStackTrace()
        ""
    }
}

fun convertTime(originalTimeStr: String): String {
    return try {
        val originalTimeFormat = SimpleDateFormat(FULL_DATE_FORMAT, Locale.getDefault())
        val targetTimeFormat = SimpleDateFormat(HOUR_MIN_AM_PM_FORMAT, Locale.getDefault())

        val calendar = Calendar.getInstance()
        calendar.time = originalTimeFormat.parse(originalTimeStr) ?: Date()

        targetTimeFormat.format(calendar.time)
    } catch (e: Exception) {
        e.printStackTrace()
        ""
    }
}

fun convertDateToMonth(inputDateString: String): String {
    return try {
        val originalDateFormat = SimpleDateFormat(FORMATTED_DATE, Locale.getDefault())
        val targetDateFormat = SimpleDateFormat(DATE_MONTH_FORMAT, Locale.getDefault())

        val calendar = Calendar.getInstance()
        calendar.time = originalDateFormat.parse(inputDateString) ?: Date()
        targetDateFormat.format(calendar.time)
    } catch (e: Exception) {
        e.printStackTrace()
        ""
    }
}


fun convertDateToFull(inputDateString: String): Date {
    return try {
        val formatter = SimpleDateFormat(DATE_MONTH_FORMAT, Locale.ENGLISH)
        val date = formatter.parse(inputDateString)
        date
    } catch (ex: ParseException) {
        ex.printStackTrace()
        val defaultDate = SimpleDateFormat(FULL_DATE_FORMAT).parse("2000-01-01")
        defaultDate
    }
}

fun convertFullDateToDate(inputDateString: String): String {
    return try {
        val originalDateFormat = SimpleDateFormat(FULL_DATE_FORMAT, Locale.getDefault())
        val targtrDateFormat = SimpleDateFormat(FORMATTED_DATE, Locale.getDefault())
        val calendar = Calendar.getInstance()
        calendar.time = originalDateFormat.parse(inputDateString) ?: Date()
        targtrDateFormat.format(calendar.time)
    } catch (e: Exception) {
        e.printStackTrace()
        ""
    }
}


fun dateFormatter(originalDateStr: Date, format: String): String {
    return try {
        val convertDate = SimpleDateFormat(format, Locale.getDefault())
        return convertDate.format(originalDateStr)
    } catch (e: Exception) {
        e.printStackTrace()
        ""
    }
}


fun convertToFormatDate(
    inputDateString: String,
    originalDateFormatStr: String,
    targetDateFormatStr: String
): String {
    return try {
        val originalDateFormat = SimpleDateFormat(originalDateFormatStr, Locale.getDefault())
        val targetDateFormat = SimpleDateFormat(targetDateFormatStr, Locale.getDefault())
        val calendar = Calendar.getInstance()
        calendar.time = originalDateFormat.parse(inputDateString) ?: Date()
        targetDateFormat.format(calendar.time)
    } catch (e: Exception) {
        e.printStackTrace()
        ""
    }
}

fun calculateAge(dateOfBirth: String): String {
    return try {
        val dateFormatter = SimpleDateFormat(FULL_DATE_FORMAT)
        val dob: Date = dateFormatter.parse(dateOfBirth)
        val calendarDob = Calendar.getInstance()
        calendarDob.time = dob
        val currentDate = Calendar.getInstance()
        val years = currentDate.get(Calendar.YEAR) - calendarDob.get(Calendar.YEAR)
        if (currentDate.get(Calendar.DAY_OF_YEAR) < calendarDob.get(Calendar.DAY_OF_YEAR)) {
            // Adjust age if birthday hasn't occurred yet this year
            return "${years - 1}"
        }
        return years.toString()

    } catch (e: java.lang.Exception) {
        e.printStackTrace()
        ""
    }

}
