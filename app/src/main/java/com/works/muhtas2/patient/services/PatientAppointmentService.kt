package com.works.muhtas2.patient.services

import com.google.firebase.firestore.FirebaseFirestore
import com.works.muhtas2.patient.models.PatientAppointmentData

class PatientAppointmentService {
    private val db = FirebaseFirestore.getInstance()

    fun getAppointmentsForPatient(patientUid: String, callback: (List<PatientAppointmentData>) -> Unit) {
        db.collection("appointments")
            .document(patientUid)
            .collection("patientAppointments")
            .get()
            .addOnSuccessListener { documents ->
                val appointmentsList = documents.mapNotNull { document ->
                    document.toObject(PatientAppointmentData::class.java)?.copy(id = document.id) // adjuntar ID del documento
                }
                callback(appointmentsList)
            }
            .addOnFailureListener {
                callback(emptyList()) // En caso de error, devolver lista vacía
            }
    }

    fun deleteAppointment(patientUid: String, doctorUid: String, appointmentId: String, callback: (Boolean) -> Unit) {
        val patientRef = db.collection("appointments")
            .document(patientUid)
            .collection("patientAppointments")
            .document(appointmentId)

        val doctorRef = db.collection("doctorAppointments")
            .document(doctorUid)
            .collection("appointments")
            .document(appointmentId)

        db.runBatch { batch ->
            batch.delete(patientRef)
            batch.delete(doctorRef)
        }.addOnSuccessListener {
            callback(true)
        }.addOnFailureListener {
            callback(false)
        }
    }
}
