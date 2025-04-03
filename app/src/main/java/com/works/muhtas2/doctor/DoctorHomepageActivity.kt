package com.works.muhtas2.doctor

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.works.muhtas2.MainActivity
import com.works.muhtas2.R
import com.works.muhtas2.doctor.adapter.DoctorAppointmentAdapter
import com.works.muhtas2.doctor.services.DoctorAppointmentService

class DoctorHomepageActivity : AppCompatActivity() {
    private lateinit var appointmentsList: ListView
    private lateinit var doctorAppointmentService: DoctorAppointmentService
    private var doctorUid: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_doctor_homepage)

        appointmentsList = findViewById(R.id.appointmentsList)
        doctorAppointmentService = DoctorAppointmentService()
        doctorUid = FirebaseAuth.getInstance().currentUser?.uid

        doctorUid?.let { uid ->
            loadAppointments(uid)
        } ?: run {
            Toast.makeText(this, "Error: No se pudo obtener la información del doctor.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadAppointments(doctorUid: String) {
        doctorAppointmentService.getAppointmentsForDoctor(doctorUid) { appointments ->
            Log.d("appointments", appointments.toString())
            val adapter = DoctorAppointmentAdapter(this, appointments)
            appointmentsList.adapter = adapter

            appointmentsList.setOnItemLongClickListener { _, _, position, _ ->
                val selectedAppointment = appointments[position]

                AlertDialog.Builder(this)
                    .setTitle("Cancelar cita")
                    .setMessage("¿Estás seguro de que quieres cancelar esta cita?")
                    .setPositiveButton("Sí") { _, _ ->
                        selectedAppointment.patientUid?.let { patientUid ->
                            selectedAppointment.id?.let { appointmentId ->
                                deleteAppointment(doctorUid, patientUid, appointmentId, adapter)
                            }
                        } ?: run {
                            Toast.makeText(this, "Error: No se pudo eliminar la cita.", Toast.LENGTH_SHORT).show()
                        }
                    }
                    .setNegativeButton("No", null)
                    .show()

                true
            }
        }
    }

    private fun deleteAppointment(doctorUid: String, patientUid: String, appointmentId: String, adapter: DoctorAppointmentAdapter) {
        doctorAppointmentService.deleteAppointment(doctorUid, patientUid, appointmentId) { success ->
            if (success) {
                Toast.makeText(this, "La cita se ha eliminado con éxito.", Toast.LENGTH_SHORT).show()
                loadAppointments(doctorUid) // Recargar la lista de citas
            } else {
                Toast.makeText(this, "Se ha producido un error al eliminar la cita.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.doctor_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.doctor_profile -> {
                startActivity(Intent(this, DoctorProfileActivity::class.java))
            }
            R.id.doctor_logout -> {
                showLogoutDialog()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun showLogoutDialog() {
        AlertDialog.Builder(this).apply {
            setTitle("Cerrar sesión")
            setMessage("¿Seguro que quieres cerrar sesión?")
            setPositiveButton("Sí") { _, _ ->
                FirebaseAuth.getInstance().signOut()
                val intent = Intent(applicationContext, MainActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                finish()
            }
            setNegativeButton("No", null)
        }.create().show()
    }
}
