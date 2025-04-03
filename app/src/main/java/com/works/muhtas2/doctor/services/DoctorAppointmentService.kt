package com.works.muhtas2.doctor.services

import com.google.firebase.firestore.FirebaseFirestore
import com.works.muhtas2.doctor.models.DoctorAppointmentData

class DoctorAppointmentService {
    private val db = FirebaseFirestore.getInstance()

    fun getAppointmentsForDoctor(
        doctorUid: String,
        callback: (List<DoctorAppointmentData>) -> Unit
    ) {
        db.collection("doctorAppointments")
            .document(doctorUid)
            .collection("appointments")
            .get()
            .addOnSuccessListener { documents ->
                val appointmentsList = documents.mapNotNull { document ->
                    val appointment = document.toObject(DoctorAppointmentData::class.java)
                        ?.copy(id = document.id)
                    appointment
                }
                callback(appointmentsList)
            }
            .addOnFailureListener {
                callback(emptyList()) // En caso de error, devolver una lista vacía
            }
    }

    fun deleteAppointment(
        doctorUid: String,
        patientUid: String,
        appointmentId: String,
        callback: (Boolean) -> Unit
    ) {
        val doctorRef = db.collection("doctorAppointments")
            .document(doctorUid)
            .collection("appointments")
            .document(appointmentId)

        val patientRef = db.collection("appointments")
            .document(patientUid)
            .collection("patientAppointments")
            .document(appointmentId)

        db.runBatch { batch ->
            batch.delete(doctorRef)
            batch.delete(patientRef)
        }.addOnSuccessListener {
            callback(true)
        }.addOnFailureListener {
            callback(false)
        }
    }
}
