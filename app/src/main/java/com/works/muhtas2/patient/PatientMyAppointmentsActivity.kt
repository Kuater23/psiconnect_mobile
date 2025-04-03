package com.works.muhtas2.patient

import android.os.Bundle
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.works.muhtas2.R
import com.works.muhtas2.patient.adapter.PatientAppointmentAdapter
import com.works.muhtas2.patient.models.PatientAppointmentData
import com.works.muhtas2.patient.services.PatientAppointmentService

class PatientMyAppointmentsActivity : AppCompatActivity() {
    private lateinit var patientAppointmentsList: ListView
    private lateinit var patientAppointmentService: PatientAppointmentService
    private lateinit var adapter: PatientAppointmentAdapter
    private var appointmentsList: MutableList<PatientAppointmentData> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_patient_my_appointments)

        patientAppointmentsList = findViewById(R.id.patientAppointmentsList)
        patientAppointmentService = PatientAppointmentService()

        val patientUid = FirebaseAuth.getInstance().currentUser?.uid
        if (patientUid != null) {
            loadAppointments(patientUid)
        } else {
            Toast.makeText(this, "Error: Usuario no autenticado", Toast.LENGTH_SHORT).show()
            finish()
        }

        patientAppointmentsList.setOnItemLongClickListener { adapterView, _, i, _ ->
            val selectedAppointment = adapterView.getItemAtPosition(i) as PatientAppointmentData
            showDeleteConfirmationDialog(selectedAppointment, patientUid!!)
            true
        }
    }

    private fun loadAppointments(patientUid: String) {
        patientAppointmentService.getAppointmentsForPatient(patientUid) { appointments ->
            runOnUiThread {
                if (appointments.isNotEmpty()) {
                    appointmentsList.clear()
                    appointmentsList.addAll(appointments)
                    adapter = PatientAppointmentAdapter(this, appointmentsList)
                    patientAppointmentsList.adapter = adapter
                } else {
                    Toast.makeText(this, "No tienes citas registradas", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun showDeleteConfirmationDialog(appointment: PatientAppointmentData, patientUid: String) {
        AlertDialog.Builder(this).apply {
            setTitle("Cancelar Cita")
            setMessage("¿Estás seguro de que deseas cancelar la cita?")
            setPositiveButton("Sí") { _, _ ->
                cancelAppointment(appointment, patientUid)
            }
            setNegativeButton("No", null)
        }.create().show()
    }

    private fun cancelAppointment(appointment: PatientAppointmentData, patientUid: String) {
        val doctorUid = appointment.doctorUid ?: return
        val appointmentId = appointment.id ?: return

        patientAppointmentService.deleteAppointment(patientUid, doctorUid, appointmentId) { success ->
            runOnUiThread {
                if (success) {
                    Toast.makeText(this, "Cita cancelada", Toast.LENGTH_SHORT).show()
                    appointmentsList.remove(appointment)
                    adapter.notifyDataSetChanged() // Refrescar la lista sin recargar el adaptador
                } else {
                    Toast.makeText(this, "No se pudo cancelar la cita", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
