package com.works.muhtas2.doctor.models

data class AppointmentData(
    val id: String? = null,
    val doctorUid: String? = null,
    val patientUid: String? = null,
    val patientFirstName: String? = null,
    val patientLastName: String? = null,
    val doctorFirstName: String? = null,
    val doctorLastName: String? = null,
    val speciality: String? = null,
    val note: String? = null,
    val date: String? = null,
    val hour: String? = null,
    val patientImageUrl: String? = null
)
