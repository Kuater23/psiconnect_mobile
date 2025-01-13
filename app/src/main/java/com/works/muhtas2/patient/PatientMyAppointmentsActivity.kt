package com.works.muhtas2.patient

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.firebase.auth.FirebaseAuth
import com.works.muhtas2.R
import com.works.muhtas2.patient.adapter.PatientAppointmentAdapter
import com.works.muhtas2.patient.models.PatientAppointmentData
import com.works.muhtas2.patient.services.PatientAppointmentService

class PatientMyAppointmentsActivity : AppCompatActivity() {
    lateinit var patientAppointmentsList: ListView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_patient_my_appointments)

        patientAppointmentsList = findViewById(R.id.patientAppointmentsList)

        val patientAppointmentService = PatientAppointmentService()
        val patientEmail = FirebaseAuth.getInstance().currentUser?.email

        patientAppointmentService.getAppointmentsForPatient(patientEmail!!) { appointments ->
            val adapter = PatientAppointmentAdapter(this, appointments)
            patientAppointmentsList.adapter = adapter
        }

        patientAppointmentsList.setOnItemLongClickListener { adapterView, _, i, _ ->
            val selectedAppointment = adapterView.getItemAtPosition(i) as PatientAppointmentData
            AlertDialog.Builder(this).apply {
                setTitle("Cancelar Cita")
                setMessage("¿Estás seguro de que deseas cancelar la cita?")
                setPositiveButton("Sí") { _, _ ->
                    // Eliminar la cita tanto de la colección de pacientes como de la de doctores
                    patientAppointmentService.deleteAppointment(
                        patientEmail,
                        selectedAppointment.doctorEmail!!,
                        selectedAppointment.id!!
                    ) { success ->
                        if (success) {
                            Toast.makeText(this@PatientMyAppointmentsActivity, "Cita cancelada", Toast.LENGTH_SHORT).show()
                            // Recargar la lista
                            patientAppointmentService.getAppointmentsForPatient(patientEmail) { updatedAppointments ->
                                val newAdapter = PatientAppointmentAdapter(this@PatientMyAppointmentsActivity, updatedAppointments)
                                patientAppointmentsList.adapter = newAdapter
                            }
                        } else {
                            Toast.makeText(this@PatientMyAppointmentsActivity, "No se pudo cancelar la cita", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                setNegativeButton("No", null)
            }.create().show()
            true
        }
    }
}