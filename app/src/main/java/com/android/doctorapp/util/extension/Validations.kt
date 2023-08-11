package com.android.doctorapp.util.extension

fun String.isEmailAddressValid(): Boolean {
    //val pattern = Regex("^[A-Za-z0-9._-]+@[a-z]+.+[a-z]+")
    //val pattern = Regex("^[A-Za-z0-9+_.-]+@(.+)\$")
    val pattern = Regex("^([a-zA-Z0-9]+)@([a-zA-Z]+)\\.([a-zA-Z]{2,5})$")
    //return this.trim().isNotEmpty() && PatternsCompat.EMAIL_ADDRESS.matcher(this).matches()
    return this.trim().isNotEmpty() && this.matches(pattern)
}

fun String.isPassWordValid(): Boolean {
    val smallLetter = Regex("[a-z]")
    val capitalLetter = Regex("[A-Z]")
    val specialLetter = Regex("[!#$@+%^*\\-=\\[\\]{};':?.<>/()_&]")

    return this.trim().isNotEmpty() && this.length >= 8 && smallLetter.containsMatchIn(this) &&
            capitalLetter.containsMatchIn(this) && specialLetter.containsMatchIn(this)
}