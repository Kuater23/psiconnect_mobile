package com.works.muhtas2.patient

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.ListView
import androidx.appcompat.app.AlertDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.works.muhtas2.MainActivity
import com.works.muhtas2.R
import com.works.muhtas2.doctor.models.DoctorData
import com.works.muhtas2.patient.adapter.DoctorCustomAdapter
import com.works.muhtas2.patient.services.DoctorService

class PatientHomePageActivity : AppCompatActivity() {
    lateinit var listView: ListView
    lateinit var doctorService: DoctorService

    var userName: String = "Usuario" // Valor predeterminado

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_patient_home_page)

        listView = findViewById(R.id.listView)
        doctorService = DoctorService()

        doctorService.getDoctors {
            val adapter = DoctorCustomAdapter(this, it)
            listView.adapter = adapter
        }

        // Obtener la imagen de perfil del usuario desde Firestore
        val userEmail = auth.currentUser?.email
        if (userEmail != null) {
            db.collection("patients").document(userEmail).get()
                .addOnSuccessListener { documentSnapshot ->
                    if (documentSnapshot.exists()) {
                        val name = documentSnapshot.getString("first") ?: ""
                        val surname = documentSnapshot.getString("last")
                        userName = "$name $surname"
                    }
                }
        }

        listView.setOnItemClickListener { adapterView, _, i, _ ->
            val selectedItem = adapterView.getItemAtPosition(i) as DoctorData
            Log.d("info", selectedItem.toString())

            val intent = Intent(this, AppointmentActivity::class.java).apply {
                putExtra("name", selectedItem.first)
                putExtra("surname", selectedItem.last)
                putExtra("age", selectedItem.age)
                putExtra("field", selectedItem.field)
                putExtra("email", selectedItem.email)
                putExtra("patientName", userName)
            }
            startActivity(intent)
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
            R.id.appointments -> {
                val intent = Intent(applicationContext, PatientMyAppointmentsActivity::class.java)
                startActivity(intent)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onResume() {
        super.onResume()
        // Recargar la lista de doctores
        doctorService.getDoctors {
            val adapter = DoctorCustomAdapter(this, it)
            listView.adapter = adapter
        }
    }
}