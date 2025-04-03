package com.works.muhtas2.doctor

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
import com.works.muhtas2.doctor.models.DoctorData

class DoctorProfileActivity : AppCompatActivity() {
    private lateinit var txtDName: TextView
    private lateinit var txtDSurname: TextView
    private lateinit var txtDdob: TextView
    private lateinit var txtDEmail: TextView
    private lateinit var txtDSpeciality: TextView
    private lateinit var btnDeleteDAccount: Button
    private lateinit var btnEditProfile: Button
    private lateinit var imgDoctorProfile: ImageView

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val user = auth.currentUser

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_doctor_profile)

        txtDName = findViewById(R.id.txtDName)
        txtDSurname = findViewById(R.id.txtDSurname)
        txtDdob = findViewById(R.id.txtDDob)
        txtDEmail = findViewById(R.id.txtDEmail)
        txtDSpeciality = findViewById(R.id.txtDSpeciality)
        btnDeleteDAccount = findViewById(R.id.btnDeleteAccount)
        btnEditProfile = findViewById(R.id.btnEditProfile)
        imgDoctorProfile = findViewById(R.id.imgDoctorProfilePicture)

        user?.uid?.let { uid ->
            loadDoctorProfile(uid)
        } ?: showToast("Error al obtener el usuario.")

        btnDeleteDAccount.setOnClickListener {
            showDeleteAccountDialog()
        }

        btnEditProfile.setOnClickListener {
            val intent = Intent(this, DoctorProfileEditActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        user?.uid?.let { loadDoctorProfile(it) }
    }

    private fun loadDoctorProfile(uid: String) {
        db.collection("doctors").document(uid)
            .get()
            .addOnSuccessListener { document ->
                val doctorData = document.toObject(DoctorData::class.java)
                if (doctorData != null) {
                    txtDName.text = "Nombre: ${doctorData.firstName ?: "N/A"}"
                    txtDSurname.text = "Apellido: ${doctorData.lastName ?: "N/A"}"
                    txtDdob.text = "Fecha de Nacimiento: ${doctorData.dob ?: "N/A"}"
                    txtDEmail.text = "Correo: ${doctorData.email ?: "N/A"}"
                    txtDSpeciality.text = "Especialidad: ${doctorData.speciality ?: "N/A"}"

                    // Si hay imagen de perfil, cargarla con Glide
                    doctorData.uid?.let { profileImageUrl ->
                        Glide.with(this)
                            .load(profileImageUrl)
                            .placeholder(android.R.drawable.ic_menu_gallery) // Placeholder predeterminado
                            .error(android.R.drawable.ic_dialog_alert) // Imagen en caso de error
                            .into(imgDoctorProfile)
                    }
                }
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error al obtener el perfil del doctor.", e)
                showToast("Error al cargar perfil.")
            }
    }

    private fun showDeleteAccountDialog() {
        AlertDialog.Builder(this)
            .setTitle("Eliminar cuenta")
            .setMessage("¿Estás seguro de que quieres eliminar tu cuenta?")
            .setPositiveButton("Sí") { _, _ ->
                user?.uid?.let { deleteAccount(it) }
            }
            .setNegativeButton("No", null)
            .show()
    }

    private fun deleteAccount(uid: String) {
        user?.delete()
            ?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    db.collection("doctors").document(uid)
                        .delete()
                        .addOnSuccessListener {
                            Log.d("Firestore", "Cuenta eliminada correctamente.")
                        }
                        .addOnFailureListener { e ->
                            Log.w("Firestore", "Error al eliminar datos de Firestore.", e)
                        }
                    showToast("Cuenta eliminada exitosamente.")
                    navigateToMain()
                } else {
                    Log.w("FirebaseAuth", "Error al eliminar la cuenta.", task.exception)
                    showToast("Error al eliminar la cuenta.")
                }
            }
    }

    private fun navigateToMain() {
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        finish()
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
