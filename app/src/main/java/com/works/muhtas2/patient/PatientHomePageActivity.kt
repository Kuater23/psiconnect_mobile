package com.works.muhtas2.patient

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
import com.google.firebase.firestore.FirebaseFirestore
import com.works.muhtas2.MainActivity
import com.works.muhtas2.R
import com.works.muhtas2.doctor.models.DoctorData
import com.works.muhtas2.patient.adapter.DoctorCustomAdapter
import com.works.muhtas2.patient.services.DoctorService

class PatientHomePageActivity : AppCompatActivity() {
    private lateinit var listView: ListView
    private lateinit var doctorService: DoctorService

    private var userName: String = "Usuario" // Valor predeterminado

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_patient_home_page)

        listView = findViewById(R.id.listView)
        doctorService = DoctorService()

        loadDoctors()
        loadUserProfile()

        listView.setOnItemClickListener { adapterView, _, i, _ ->
            val selectedItem = adapterView.getItemAtPosition(i) as DoctorData
            Log.d("info", selectedItem.toString())

            val intent = Intent(this, AppointmentActivity::class.java).apply {
                putExtra("firstName", selectedItem.firstName)
                putExtra("lastName", selectedItem.lastName)
                putExtra("dob", selectedItem.dob)
                putExtra("speciality", selectedItem.speciality)
                putExtra("doctorUid", selectedItem.uid)
                putExtra("doctorImage", selectedItem.license)
                putExtra("patientName", userName)
            }
            startActivity(intent)
        }
    }

    private fun loadDoctors() {
        doctorService.getDoctors { doctorList ->
            runOnUiThread {
                if (doctorList.isNotEmpty()) {
                    val adapter = DoctorCustomAdapter(this, doctorList)
                    listView.adapter = adapter
                } else {
                    Toast.makeText(this, "No hay doctores disponibles", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun loadUserProfile() {
        val userUid = auth.currentUser?.uid
        if (userUid != null) {
            db.collection("patients").document(userUid).get()
                .addOnSuccessListener { documentSnapshot ->
                    if (documentSnapshot.exists()) {
                        val name = documentSnapshot.getString("firstName") ?: ""
                        val surname = documentSnapshot.getString("lastName") ?: ""
                        userName = "$name $surname"
                    }
                }
                .addOnFailureListener {
                    Log.e("Firestore", "Error al obtener perfil del usuario", it)
                }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.patient_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.profile -> {
                val intent = Intent(this, PatientProfileActivity::class.java)
                startActivity(intent)
            }

            R.id.logout -> {
                showLogoutDialog()
            }

            R.id.appointments -> {
                val intent = Intent(this, PatientMyAppointmentsActivity::class.java)
                startActivity(intent)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun showLogoutDialog() {
        AlertDialog.Builder(this).apply {
            setTitle("Cerrar sesión")
            setMessage("¿Estás seguro de que deseas cerrar sesión?")
            setPositiveButton("Sí") { _, _ ->
                FirebaseAuth.getInstance().signOut()
                val intent = Intent(applicationContext, MainActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                startActivity(intent)
                finish()
            }
            setNegativeButton("No", null)
        }.create().show()
    }

    override fun onResume() {
        super.onResume()
        loadDoctors()
    }
}
