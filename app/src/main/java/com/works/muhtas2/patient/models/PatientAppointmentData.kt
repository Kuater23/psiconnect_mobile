package com.works.muhtas2.patient.models

data class PatientAppointmentData(
    val id: String? = null,
    val patientUid: String? = null,
    val doctorUid: String? = null,
    val doctorFirstName: String? = null,
    val doctorLastName: String? = null,
    val speciality: String? = null,
    val doctorImageUrl: String? = null,
    val note: String? = null,
    val date: String? = null,
    val hour: String? = null
)
