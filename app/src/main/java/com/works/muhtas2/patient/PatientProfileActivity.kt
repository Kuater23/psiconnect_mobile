package com.works.muhtas2.patient

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.works.muhtas2.MainActivity
import com.works.muhtas2.R
import com.works.muhtas2.patient.models.PatientData

class PatientProfileActivity : AppCompatActivity() {
    private lateinit var txtPName: TextView
    private lateinit var txtPSurname: TextView
    private lateinit var txtPDob: TextView
    private lateinit var txtPEmail: TextView
    private lateinit var btnDeleteAccount: Button
    private lateinit var btnEditProfile: Button
    private lateinit var imgPatientProfile: ImageView

    private val db = FirebaseFirestore.getInstance()
    private val user = FirebaseAuth.getInstance().currentUser

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_patient_profile)

        txtPName = findViewById(R.id.txtPName)
        txtPSurname = findViewById(R.id.txtPSurname)
        txtPDob = findViewById(R.id.txtPDob)
        txtPEmail = findViewById(R.id.txtPEmail)
        btnDeleteAccount = findViewById(R.id.btnDeleteAccount)
        btnEditProfile = findViewById(R.id.btnEditProfile)
        imgPatientProfile = findViewById(R.id.imgPatientProfilePicture)

        loadUserProfile()

        btnDeleteAccount.setOnClickListener {
            showDeleteConfirmationDialog()
        }

        btnEditProfile.setOnClickListener {
            val intent = Intent(this, PatientProfileEditActivity::class.java)
            startActivity(intent)
        }
    }

    private fun loadUserProfile() {
        user?.uid?.let { uid ->
            db.collection("patients").document(uid)
                .get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val patient = document.toObject(PatientData::class.java)
                        runOnUiThread {
                            txtPName.text = "Nombre: ${patient?.firstName ?: "N/A"}"
                            txtPSurname.text = "Apellido: ${patient?.lastName ?: "N/A"}"
                            txtPDob.text = "Fecha de Nacimiento: ${patient?.dob ?: "N/A"}"
                            txtPEmail.text = "Correo electrónico: ${patient?.email ?: "N/A"}"

                            // Cargar imagen con Glide o mostrar una imagen por defecto
                            Glide.with(this)
                                .load(patient?.phoneN)
                                .placeholder(android.R.drawable.ic_menu_camera) // Ícono genérico de Android
                                .error(android.R.drawable.ic_menu_camera) // Se muestra si no hay imagen
                                .into(imgPatientProfile)
                        }
                    } else {
                        Log.e("Firestore", "No se encontró el documento del paciente.")
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("Firestore", "Error al obtener datos del paciente: ${e.message}", e)
                }
        }
    }

    private fun showDeleteConfirmationDialog() {
        AlertDialog.Builder(this).apply {
            setTitle("Eliminar cuenta")
            setMessage("¿Estás seguro de que deseas eliminar tu cuenta?")
            setPositiveButton("Sí") { _, _ -> deleteAccount() }
            setNegativeButton("No", null)
        }.create().show()
    }

    private fun deleteAccount() {
        user?.uid?.let { uid ->
            db.collection("patients").document(uid)
                .delete()
                .addOnSuccessListener {
                    Log.d("Firestore", "Cuenta eliminada en Firestore")
                    deleteAuthAccount()
                }
                .addOnFailureListener { e ->
                    Log.e("Firestore", "Error al eliminar datos del paciente: ${e.message}", e)
                    Toast.makeText(this, "Error al eliminar la cuenta", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun deleteAuthAccount() {
        user?.delete()?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.d("FirebaseAuth", "Cuenta eliminada exitosamente")
                Toast.makeText(this, "Cuenta eliminada correctamente", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            } else {
                Log.e("FirebaseAuth", "Error al eliminar la cuenta de Firebase Auth", task.exception)
                Toast.makeText(this, "Error al eliminar la cuenta en Firebase Auth", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        loadUserProfile()
    }
}
