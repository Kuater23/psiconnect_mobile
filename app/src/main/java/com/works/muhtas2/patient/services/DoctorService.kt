package com.works.muhtas2.patient.services

import com.google.firebase.firestore.FirebaseFirestore
import com.works.muhtas2.doctor.models.DoctorData

class DoctorService {

    private val db = FirebaseFirestore.getInstance()
    private val doctorsRef = db.collection("doctors")

    fun getDoctors(callback: (List<DoctorData>) -> Unit) {
        doctorsRef.get()
            .addOnSuccessListener { querySnapshot ->
                val doctorDataList = querySnapshot.documents.mapNotNull { it.toObject(DoctorData::class.java) }
                callback(doctorDataList)
            }
            .addOnFailureListener {
                callback(emptyList()) // En caso de error, devuelve una lista vacía
            }
    }
}
