package com.android.doctorapp.repository.models

import com.google.firebase.firestore.DocumentSnapshot
import java.util.Date

data class PaginatedResponse(
    val data: List<AppointmentModel>,
    val lastDocument: DocumentSnapshot? // Include the last document in the response
)
